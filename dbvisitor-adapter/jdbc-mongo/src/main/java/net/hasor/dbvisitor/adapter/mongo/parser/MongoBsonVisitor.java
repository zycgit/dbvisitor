/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.adapter.mongo.parser;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.JdbcArg;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.*;

public class MongoBsonVisitor extends MongoParserBaseVisitor<Object> {
    private final AdapterRequest request;
    private final AtomicInteger  argIndex;

    public MongoBsonVisitor(AdapterRequest request, AtomicInteger argIndex) {
        this.request = request;
        this.argIndex = argIndex;
    }

    protected Object getArg() {
        int argIdx = this.argIndex.getAndIncrement();
        String argName = "arg" + (argIdx + 1);
        JdbcArg jdbcArg = this.request.getArgMap().get(argName);
        if (jdbcArg == null) {
            throw new RuntimeException(argName + " not found in request.");
        } else {
            return jdbcArg.getValue();
        }
    }

    protected String getIdentifier(String nodeText) {
        char firstChar = nodeText.charAt(0);
        char endChar = nodeText.charAt(nodeText.length() - 1);
        if (firstChar == '"' && endChar == '"') {
            return nodeText.substring(1, nodeText.length() - 1);
        } else if (firstChar == '\'' && endChar == '\'') {
            return nodeText.substring(1, nodeText.length() - 1);
        } else {
            return nodeText;
        }
    }

    @Override
    public Object visitObjectLiteral(MongoParser.ObjectLiteralContext ctx) {
        Document document = new Document();
        if (ctx.propertyNameAndValueList() != null) {
            for (MongoParser.PropertyAssignmentContext assignment : ctx.propertyNameAndValueList().propertyAssignment()) {
                String key = (String) visit(assignment.propertyName());
                Object value = visit(assignment.propertyValue());
                document.put(key, value);
            }
        }
        return document;
    }

    @Override
    public Object visitArrayLiteral(MongoParser.ArrayLiteralContext ctx) {
        List<Object> list = new ArrayList<>();
        if (ctx.elementList() != null) {
            for (MongoParser.PropertyValueContext valueCtx : ctx.elementList().propertyValue()) {
                list.add(visit(valueCtx));
            }
        }
        return list;
    }

    @Override
    public Object visitPropertyName(MongoParser.PropertyNameContext ctx) {
        if (ctx.ARG() != null) {
            return String.valueOf(getArg());
        }
        return getIdentifier(ctx.getText());
    }

    @Override
    public Object visitLiteral(MongoParser.LiteralContext ctx) {
        if (ctx.ARG() != null) {
            return getArg();
        }
        if (ctx.NUMERIC_LITERAL() != null) {
            String text = ctx.NUMERIC_LITERAL().getText();
            if (text.contains(".") || text.contains("e") || text.contains("E")) {
                return Double.parseDouble(text);
            }
            return Long.parseLong(text);
        }
        if (ctx.DOUBLE_QUOTED_STRING_LITERAL() != null || ctx.SINGLE_QUOTED_STRING_LITERAL() != null) {
            return getIdentifier(ctx.getText());
        }
        if (ctx.TRUE() != null) {
            return Boolean.TRUE;
        }
        if (ctx.FALSE() != null) {
            return Boolean.FALSE;
        }
        if (ctx.NULL() != null) {
            return null;
        }
        return ctx.getText();
    }

    @Override
    public Object visitPropertyValue(MongoParser.PropertyValueContext ctx) {
        if (ctx.literal() != null) {
            return visit(ctx.literal());
        }
        if (ctx.arrayLiteral() != null) {
            return visit(ctx.arrayLiteral());
        }
        if (ctx.objectLiteral() != null) {
            return visit(ctx.objectLiteral());
        }
        if (ctx.bsonLiteral() != null) {
            return visit(ctx.bsonLiteral());
        }
        if (ctx.functionCall() != null) {
            return visit(ctx.functionCall());
        }
        return super.visitPropertyValue(ctx);
    }

    @Override
    public Object visitFunctionCall(MongoParser.FunctionCallContext ctx) {
        String funcName = ctx.identifier().getText();
        List<Object> args = (List<Object>) visit(ctx.arguments());
        Object arg0 = args.size() > 0 ? args.get(0) : null;

        if ("ObjectId".equalsIgnoreCase(funcName)) {
            return convertObjectId(arg0);
        }
        if ("ISODate".equalsIgnoreCase(funcName)) {
            return arg0 == null ? new Date() : new Date(arg0.toString()); // TODO: better ISO parsing
        }
        if ("NumberInt".equalsIgnoreCase(funcName)) {
            return arg0 instanceof Number ? ((Number) arg0).intValue() : Integer.parseInt(arg0.toString());
        }
        if ("NumberLong".equalsIgnoreCase(funcName)) {
            return arg0 instanceof Number ? ((Number) arg0).longValue() : Long.parseLong(arg0.toString());
        }
        if ("NumberDecimal".equalsIgnoreCase(funcName)) {
            return new Decimal128(new java.math.BigDecimal(arg0.toString()));
        }
        if ("Timestamp".equalsIgnoreCase(funcName)) {
            int time = args.size() > 0 ? ((Number) args.get(0)).intValue() : 0;
            int inc = args.size() > 1 ? ((Number) args.get(1)).intValue() : 0;
            return new BSONTimestamp(time, inc);
        }
        if ("UUID".equalsIgnoreCase(funcName)) {
            return java.util.UUID.fromString(arg0.toString());
        }
        if ("MinKey".equalsIgnoreCase(funcName)) {
            return new MinKey();
        }
        if ("MaxKey".equalsIgnoreCase(funcName)) {
            return new MaxKey();
        }
        // fallback
        return super.visitFunctionCall(ctx);
    }

    @Override
    public Object visitBsonLiteral(MongoParser.BsonLiteralContext ctx) {
        List<Object> args = (List<Object>) visit(ctx.arguments());
        Object arg0 = args.size() > 0 ? args.get(0) : null;

        if (ctx.OBJECT_ID() != null) {
            return convertObjectId(arg0);
        }
        if (ctx.ISO_DATE() != null) {
            // Simple implementation, might need better parsing for ISO strings
            return arg0 == null ? new Date() : new Date(arg0.toString()); // TODO: Parse ISO String
        }
        if (ctx.NUMBER_INT() != null) {
            return arg0 instanceof Number ? ((Number) arg0).intValue() : Integer.parseInt(arg0.toString());
        }
        if (ctx.NUMBER_LONG() != null) {
            return arg0 instanceof Number ? ((Number) arg0).longValue() : Long.parseLong(arg0.toString());
        }
        if (ctx.NUMBER_DECIMAL() != null) {
            return new Decimal128(new java.math.BigDecimal(arg0.toString()));
        }
        if (ctx.TIMESTAMP() != null) {
            // Timestamp(time, inc)
            int time = args.size() > 0 ? ((Number) args.get(0)).intValue() : 0;
            int inc = args.size() > 1 ? ((Number) args.get(1)).intValue() : 0;
            return new BSONTimestamp(time, inc);
        }
        if (ctx.UUID() != null) {
            return java.util.UUID.fromString(arg0.toString());
        }
        if (ctx.MIN_KEY() != null) {
            return new MinKey();
        }
        if (ctx.MAX_KEY() != null) {
            return new MaxKey();
        }
        // Add other types as needed
        return null;
    }

    private Object convertObjectId(Object arg0) {
        if (arg0 == null) {
            return new ObjectId();
        }

        if (arg0 instanceof ObjectId) {
            return arg0;
        }
        if (arg0 instanceof BsonValue) {
            BsonValue bsonVal = (BsonValue) arg0;
            if (bsonVal.isObjectId()) {
                return bsonVal.asObjectId().getValue();
            }
            if (bsonVal.isString() && ObjectId.isValid(bsonVal.asString().getValue())) {
                return new ObjectId(bsonVal.asString().getValue());
            }
        }

        if (arg0 instanceof byte[]) {
            byte[] bytes = (byte[]) arg0;
            if (bytes.length == 12) {
                return new ObjectId(bytes);
            }
        }

        String oidText = String.valueOf(arg0);
        if (ObjectId.isValid(oidText)) {
            return new ObjectId(oidText);
        }
        return new ObjectId(oidText);
    }

    @Override
    public Object visitArguments(MongoParser.ArgumentsContext ctx) {
        if (ctx.argumentList() != null) {
            return visit(ctx.argumentList());
        }
        return new ArrayList<>();
    }

    @Override
    public Object visitArgumentList(MongoParser.ArgumentListContext ctx) {
        List<Object> list = new ArrayList<>();
        if (ctx.children != null) {
            for (org.antlr.v4.runtime.tree.ParseTree child : ctx.children) {
                if (child instanceof MongoParser.PropertyValueContext) {
                    list.add(visit(child));
                }
            }
        }
        return list;
    }
}
