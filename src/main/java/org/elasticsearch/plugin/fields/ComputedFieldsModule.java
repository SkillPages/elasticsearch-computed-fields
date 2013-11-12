package org.elasticsearch.plugin.fields;

import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.index.fields.RegisterComputedFieldType;

public class ComputedFieldsModule extends AbstractModule
{
    @Override
    protected void configure() 
    {
        bind(RegisterComputedFieldType.class).asEagerSingleton();
    }
}
