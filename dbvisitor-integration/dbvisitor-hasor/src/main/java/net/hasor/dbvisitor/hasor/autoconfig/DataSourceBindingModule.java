/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.hasor.autoconfig;

import java.beans.PropertyDescriptor;
import javax.sql.DataSource;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.ApiBinder;
import net.hasor.core.BindInfo;

/** Creates a settings-based data source only when the container has not already registered one. */
class DataSourceBindingModule implements net.hasor.core.Module {
    private final String sourceName;

    DataSourceBindingModule(String sourceName) {
        this.sourceName = sourceName;
    }

    @Override
    public void loadModule(ApiBinder apiBinder) throws Exception {
        if (apiBinder.findBindingRegister(this.bindingName(), DataSource.class) != null) {
            return;
        }

        Settings settings = apiBinder.getSettings();
        AutoConfigSettings configuration = new AutoConfigSettings(settings, this.sourceName);
        String configKey = configuration.getDataSourceConfigKey();
        String dataSourceType = configuration.getDataSourceType();
        DataSource dataSource;
        if (StringUtils.isBlank(dataSourceType)) {
            dataSource = new DefaultDataSource();
        } else {
            Class<?> dsClass = apiBinder.getClassLoader().loadClass(dataSourceType);
            dataSource = ClassUtils.newInstance(dsClass.asSubclass(DataSource.class));
        }

        this.applySettings(configuration, configKey, dataSource);
        if (StringUtils.isBlank(this.sourceName)) {
            apiBinder.bindType(DataSource.class).toInstance(dataSource);
        } else {
            apiBinder.bindType(DataSource.class).nameWith(this.sourceName).toInstance(dataSource);
        }
    }

    private String bindingName() {
        return this.sourceName == null ? "" : this.sourceName;
    }

    private void applySettings(AutoConfigSettings settings, String configKey, DataSource dataSource) {
        for (PropertyDescriptor descriptor : BeanUtils.getPropertyDescriptors(dataSource.getClass())) {
            String propertyName = descriptor.getName();
            if ("class".equals(propertyName)) {
                continue;
            }
            String settingKey = configKey + "." + StringUtils.humpToLine(propertyName).replace('_', '-');
            String propertyValue = settings.getString(settingKey, null);
            if (StringUtils.isNotBlank(propertyValue)) {
                BeanUtils.writeProperty(dataSource, propertyName, propertyValue);
            }
        }
    }
}
