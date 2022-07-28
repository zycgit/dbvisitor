package net.hasor.dbvisitor.faker.generator;

import java.sql.SQLException;
import java.util.List;

public interface Action {
    List<BoundQuery> generatorAction(int batchSize) throws SQLException;
}
