package net.hasor.scene;
import net.hasor.dbvisitor.keyholder.CreateContext;
import net.hasor.dbvisitor.keyholder.KeySeqHolder;
import net.hasor.dbvisitor.keyholder.KeySeqHolderFactory;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;

import java.sql.Connection;

public class MyKeySeqHolder implements KeySeqHolderFactory {

    @Override
    public KeySeqHolder createHolder(CreateContext context) {
        return new KeySeqHolder() {
            @Override
            public boolean onBefore() {
                return true;
            }

            @Override
            public Object beforeApply(Connection conn, Object entity, ColumnMapping mapping) {
                mapping.getHandler().set(entity, 111111);
                return 111111;
            }
        };
    }
}
