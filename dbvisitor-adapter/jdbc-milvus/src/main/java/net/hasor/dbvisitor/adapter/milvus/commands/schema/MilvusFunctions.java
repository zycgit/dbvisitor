package net.hasor.dbvisitor.adapter.milvus.commands.schema;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.milvus.common.clientenum.FunctionType;
import io.milvus.v2.common.DataType;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.FunctionDefinitionContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.IdentifierContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.IdentifiersContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.PropertiesListContext;
import net.hasor.dbvisitor.driver.AdapterRequest;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readName;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readProperties;

/** Analyzer settings and server-generated function fields in collection schemas. */
final class MilvusFunctions {
    private MilvusFunctions() {
    }

    static void configureField(CreateCollectionReq.FieldSchema field, PropertiesListContext options, AtomicInteger args, AdapterRequest request) throws SQLException {
        Map<String, Object> properties = readProperties(args, request, options);
        if (properties.isEmpty())
            return;
        if (field.getDataType() != DataType.VarChar)
            throw new SQLException("Analyzer options require a VARCHAR field.");
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (MilvusCommandKeys.ENABLE_ANALYZER.equals(key) || MilvusCommandKeys.ENABLE_MATCH.equals(key)) {
                if (!(value instanceof Boolean))
                    throw new SQLException(key + " requires true or false.");
                if (MilvusCommandKeys.ENABLE_ANALYZER.equals(key))
                    field.setEnableAnalyzer((Boolean) value);
                else
                    field.setEnableMatch((Boolean) value);
            } else if (MilvusCommandKeys.ANALYZER_PARAMS.equals(key)) {
                try {
                    JsonObject object = JsonParser.parseString(String.valueOf(value)).getAsJsonObject();
                    field.setAnalyzerParams(new Gson().fromJson(object, Map.class));
                } catch (RuntimeException e) {
                    throw new SQLException(MilvusCommandKeys.ANALYZER_PARAMS + " requires a JSON object string.", e);
                }
            } else
                throw new SQLException("Unknown field option: " + key);
        }
    }

    static void addFunctions(CreateCollectionReq.CollectionSchema schema, List<FunctionDefinitionContext> definitions, AtomicInteger args, AdapterRequest request) throws SQLException {
        Map<String, CreateCollectionReq.FieldSchema> fields = new LinkedHashMap<>();
        for (CreateCollectionReq.FieldSchema field : schema.getFieldSchemaList()) {
            if (fields.put(field.getName(), field) != null)
                throw new SQLException("Duplicate field: " + field.getName());
        }
        Set<String> names = new HashSet<>();
        Set<String> generated = new HashSet<>();
        for (FunctionDefinitionContext definition : definitions) {
            String name = readName(definition.name);
            if (!names.add(name))
                throw new SQLException("Duplicate function: " + name);
            FunctionType type;
            try {
                type = FunctionType.valueOf(readName(definition.type).toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new SQLException("Unknown function type: " + readName(definition.type), e);
            }
            if (type != FunctionType.BM25 && type != FunctionType.TEXTEMBEDDING)
                throw new SQLException("Collection functions support BM25 and TEXTEMBEDDING.");
            List<String> inputs = fieldNames(definition.inputs);
            List<String> outputs = fieldNames(definition.outputs);
            if (inputs.size() != 1 || outputs.size() != 1)
                throw new SQLException(type + " requires one input and one output field.");
            CreateCollectionReq.FieldSchema input = fields.get(inputs.get(0));
            CreateCollectionReq.FieldSchema output = fields.get(outputs.get(0));
            if (input == null || output == null || input.getDataType() != DataType.VarChar || input.getIsNullable()) {
                throw new SQLException(type + " requires an existing non-nullable VARCHAR input and a vector output.");
            }
            boolean bm25 = type == FunctionType.BM25;
            if (bm25 && (!Boolean.TRUE.equals(input.getEnableAnalyzer()) || output.getDataType() != DataType.SparseFloatVector)) {
                throw new SQLException("BM25 requires " + MilvusCommandKeys.ENABLE_ANALYZER + "=true and SPARSE_FLOAT_VECTOR output.");
            }
            if (!bm25 && output.getDataType() != DataType.FloatVector && output.getDataType() != DataType.Float16Vector && output.getDataType() != DataType.BFloat16Vector)
                throw new SQLException("TEXTEMBEDDING requires a dense float vector output.");
            if (!generated.add(output.getName()))
                throw new SQLException("Multiple functions cannot write the same field: " + output.getName());
            Map<String, String> params = new LinkedHashMap<>();
            readProperties(args, request, definition.propertiesList()).forEach((key, value) -> params.put(key, String.valueOf(value)));
            schema.getFunctionList().add(CreateCollectionReq.Function.builder().name(name).functionType(type).inputFieldNames(inputs).outputFieldNames(outputs).params(params).build());
        }
    }

    private static List<String> fieldNames(IdentifiersContext fields) {
        List<String> names = new ArrayList<>();
        for (IdentifierContext field : fields.identifier())
            names.add(readName(field));
        return names;
    }

    static void appendFieldOptions(StringBuilder sql, io.milvus.grpc.FieldSchema field) {
        Map<String, String> options = new LinkedHashMap<>();
        field.getTypeParamsList().forEach(p -> {
            if (Arrays.asList(MilvusCommandKeys.ENABLE_ANALYZER, MilvusCommandKeys.ENABLE_MATCH, MilvusCommandKeys.ANALYZER_PARAMS).contains(p.getKey()))
                options.put(p.getKey(), p.getValue());
        });
        appendOptions(sql, options, true);
    }

    static void appendFunctions(StringBuilder sql, CreateCollectionReq.CollectionSchema schema) {
        for (CreateCollectionReq.Function function : schema.getFunctionList()) {
            sql.append(", FUNCTION ").append(function.getName()).append(" USING ").append(function.getFunctionType().name()).append(" (").append(String.join(", ", function.getInputFieldNames())).append(") INTO (").append(String.join(", ", function.getOutputFieldNames())).append(')');
            appendOptions(sql, function.getParams(), false);
        }
    }

    private static void appendOptions(StringBuilder sql, Map<String, String> options, boolean field) {
        if (options.isEmpty())
            return;
        sql.append(" WITH (");
        boolean comma = false;
        for (Map.Entry<String, String> entry : options.entrySet()) {
            if (comma)
                sql.append(", ");
            comma = true;
            sql.append(entry.getKey()).append('=');
            boolean booleanOption = MilvusCommandKeys.ENABLE_ANALYZER.equals(entry.getKey()) || MilvusCommandKeys.ENABLE_MATCH.equals(entry.getKey());
            if (field && booleanOption)
                sql.append(entry.getValue());
            else
                sql.append('\'').append(entry.getValue().replace("'", "''")).append('\'');
        }
        sql.append(')');
    }
}
