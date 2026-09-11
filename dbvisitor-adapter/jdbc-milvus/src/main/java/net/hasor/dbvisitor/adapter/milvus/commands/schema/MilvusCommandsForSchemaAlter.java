package net.hasor.dbvisitor.adapter.milvus.commands.schema;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.v2.service.collection.request.*;
import io.milvus.v2.service.database.request.AlterDatabasePropertiesReq;
import io.milvus.v2.service.database.request.DropDatabasePropertiesReq;
import io.milvus.v2.service.index.request.AlterIndexPropertiesReq;
import io.milvus.v2.service.index.request.DropIndexPropertiesReq;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.AlterCmdContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.HintCommandContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.PropertyNameContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import static net.hasor.dbvisitor.adapter.milvus.MilvusRequest.checkActive;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.*;

/** Online schema changes. Each SQL command maps to one native SDK mutation. */
public final class MilvusCommandsForSchemaAlter extends MilvusCommands {
    private MilvusCommandsForSchemaAlter() {
    }

    public static Future<?> execAlter(Future<Object> future, MilvusCmd cmd, HintCommandContext hints, AlterCmdContext context, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, hints.hint());
        if (context.functionDefinition() != null || context.functionName != null) {
            alterFunction(cmd, context, argIndex, request);
        } else if (context.ADD() != null) {
            AddCollectionFieldReq field = MilvusFieldDefinition.readAddedField(context.fieldDefinition(), argIndex, request);
            field.setDatabaseName(cmd.getCatalog());
            field.setCollectionName(readName(context.collectionName));
            cmd.addCollectionField(field);
        } else if (context.DROP() != null) {
            dropProperties(cmd, context, propertyNames(context));
        } else {
            setProperties(cmd, context, readStringProperties(argIndex, request, context.propertiesList()));
        }
        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    private static void alterFunction(MilvusCmd cmd, AlterCmdContext context, AtomicInteger args, AdapterRequest request) throws SQLException {
        String collection = MilvusFunctions.functionIdentifier(context.collectionName);
        checkActive(request);
        if (context.functionName != null) {
            String name = MilvusFunctions.functionIdentifier(context.functionName);
            cmd.dropCollectionFunction(DropCollectionFunctionReq.builder().databaseName(cmd.getCatalog()).collectionName(collection).functionName(name).build());
        } else {
            CreateCollectionReq.Function function = MilvusFunctions.readFunction(context.functionDefinition(), args, request);
            if (context.ADD() != null) {
                cmd.addCollectionFunction(AddCollectionFunctionReq.builder().databaseName(cmd.getCatalog()).collectionName(collection).function(function).build());
            } else {
                cmd.alterCollectionFunction(AlterCollectionFunctionReq.builder().databaseName(cmd.getCatalog()).collectionName(collection).function(function).build());
            }
        }
        checkActive(request);
    }

    private static void setProperties(MilvusCmd cmd, AlterCmdContext context, Map<String, String> properties) throws SQLException {
        if (context.DATABASE() != null) {
            cmd.alterDatabaseProperties(AlterDatabasePropertiesReq.builder().databaseName(readDatabaseName(context.dbName, cmd)).properties(properties).build());
            return;
        }
        String collection = readName(context.collectionName);
        if (context.INDEX() != null) {
            cmd.alterIndexProperties(AlterIndexPropertiesReq.builder().databaseName(cmd.getCatalog()).collectionName(collection).indexName(readName(context.indexName)).properties(properties).build());
        } else if (context.fieldName != null) {
            cmd.alterCollectionField(AlterCollectionFieldReq.builder().databaseName(cmd.getCatalog()).collectionName(collection).fieldName(readName(context.fieldName)).properties(properties).build());
        } else {
            cmd.alterCollectionProperties(AlterCollectionPropertiesReq.builder().databaseName(cmd.getCatalog()).collectionName(collection).properties(properties).build());
        }
    }

    private static void dropProperties(MilvusCmd cmd, AlterCmdContext context, List<String> names) throws SQLException {
        if (context.DATABASE() != null) {
            cmd.dropDatabaseProperties(DropDatabasePropertiesReq.builder().databaseName(readDatabaseName(context.dbName, cmd)).propertyKeys(names).build());
            return;
        }
        String collection = readName(context.collectionName);
        if (context.INDEX() != null) {
            cmd.dropIndexProperties(DropIndexPropertiesReq.builder().databaseName(cmd.getCatalog()).collectionName(collection).indexName(readName(context.indexName)).propertyKeys(names).build());
        } else if (context.fieldName != null) {
            cmd.dropCollectionFieldProperties(DropCollectionFieldPropertiesReq.builder().databaseName(cmd.getCatalog()).collectionName(collection).fieldName(readName(context.fieldName)).propertyKeys(names).build());
        } else {
            cmd.dropCollectionProperties(DropCollectionPropertiesReq.builder().databaseName(cmd.getCatalog()).collectionName(collection).propertyKeys(names).build());
        }
    }

    private static List<String> propertyNames(AlterCmdContext context) throws SQLException {
        List<String> names = new ArrayList<>();
        for (PropertyNameContext property : context.propertyNames().propertyName()) {
            String name = getIdentifier(property.getText());
            if (name.trim().isEmpty() || "?".equals(name)) {
                throw new SQLException("Property key must be a non-empty name, not a parameter.");
            }
            names.add(name);
        }
        return names;
    }
}
