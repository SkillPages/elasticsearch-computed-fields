package org.elasticsearch.index.fields;

import org.elasticsearch.index.Index;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.AbstractIndexComponent;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.script.ScriptService;

public class RegisterComputedFieldType extends AbstractIndexComponent
{
    @Inject
    protected RegisterComputedFieldType(Index index, Settings indexSettings, MapperService mapperService, ScriptService scriptService)
    {
        super(index, indexSettings);
        
        mapperService.documentMapperParser().putTypeParser("computed", new ComputedFieldMapper.TypeParser(scriptService));
        mapperService.documentMapperParser().putRootTypeParser("computed_fields", new ComputedFieldRootMapper.TypeParser());
    }
}
