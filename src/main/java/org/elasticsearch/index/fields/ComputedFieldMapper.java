package org.elasticsearch.index.fields;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.FieldType;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.helpers.fields.SettingsHelper;
import org.elasticsearch.index.mapper.*;
import org.elasticsearch.index.mapper.core.*;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;
import org.elasticsearch.index.mapper.ip.IpFieldMapper;
import org.elasticsearch.script.CompiledScript;
import org.elasticsearch.script.ScriptService;

public class ComputedFieldMapper implements Mapper
{
    private final String _name;
    private final ScriptService _scriptService;
   
    private Map<String, Object> _mappings;   
    private Mapper _resultMapper;
    private CompiledScript _script;
    private boolean _externalValueSupported;
    private boolean _deleted;
    
    private ComputedFieldRootMapper _root;
    private final ESLogger _logger;
    
    protected ComputedFieldMapper(String name, ScriptService scriptService, Map<String, Object> mappings, Mapper resultMapper)
    {
        _logger = Loggers.getLogger("computed-fields", SettingsHelper.GetSettings(), name);
        
        _name = name;
        _scriptService = scriptService;
        _mappings = mappings;     
        _resultMapper = resultMapper;
        
        String lang = XContentMapValues.nodeStringValue(mappings.get("lang"), null);
        String script = XContentMapValues.nodeStringValue(mappings.get("script"), null);
        
        if (script != null) _script = scriptService.compile(lang, script);
        else _script = null;
        
        boolean externalValueSupported = false;
        
        if (_resultMapper instanceof StringFieldMapper) externalValueSupported = true;
        else if (_resultMapper instanceof ByteFieldMapper) externalValueSupported = true;
        else if (_resultMapper instanceof ShortFieldMapper) externalValueSupported = true;
        else if (_resultMapper instanceof IntegerFieldMapper) externalValueSupported = true;
        else if (_resultMapper instanceof LongFieldMapper) externalValueSupported = true;
        else if (_resultMapper instanceof FloatFieldMapper) externalValueSupported = true;
        else if (_resultMapper instanceof DoubleFieldMapper) externalValueSupported = true;
        //else if (_resultMapper instanceof BooleanFieldMapper) externalValueSupported = true;
        //else if (_resultMapper instanceof BinaryFieldMapper) externalValueSupported = true;
        else if (_resultMapper instanceof DateFieldMapper) externalValueSupported = true;
        else if (_resultMapper instanceof IpFieldMapper) externalValueSupported = true;
        //else if (_resultMapper instanceof ObjectMapper) externalValueSupported = true;
        //else if (_resultMapper instanceof CompletionFieldMapper) externalValueSupported = true;
        //else if (_resultMapper instanceof MultiFieldMapper) externalValueSupported = true;
        //else if (_resultMapper instanceof GeoPointFieldMapper) externalValueSupported = true;
        
        _externalValueSupported = externalValueSupported;
    }
    
    @Override
    public String name()
    {
        return _name;
    }
    
    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException
    {
        if (_deleted) return builder;
        if (_root == null) ComputedFieldRootMapper.Register(builder, this);
        
        builder.field(_name, _mappings);
        return builder;
    }

    @Override
    public void parse(ParseContext context) throws IOException
    {
    }

    @Override
    public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException
    {
        try
        {
            ComputedFieldMapper m = (ComputedFieldMapper)mergeWith;
            if (!mergeContext.mergeFlags().simulate())
            {
                _mappings = m._mappings;
                Mapper mm = _resultMapper;
                _resultMapper = m._resultMapper;
                m._resultMapper = mm;
                _script = m._script;
                _externalValueSupported = m._externalValueSupported;
                _deleted = m._deleted;
                
                reset();           
            }
            m.close();
        }
        catch (Throwable ex)
        {
            _logger.error("field mapper merge", ex);
            if (ex instanceof RuntimeException) throw (RuntimeException)ex;
            else throw new RuntimeException(ex);
        }
    }

    @Override
    public void traverse(FieldMapperListener fieldMapperListener)
    {
        try
        {
            if (_resultMapper == null) return;
            if (_deleted) return;
                 
            _resultMapper.traverse(fieldMapperListener);
        }
        catch (Throwable ex)
        {
            _logger.error("field mapper traverse", ex);
            if (ex instanceof RuntimeException) throw (RuntimeException)ex;
            else throw new RuntimeException(ex);
        }
    }

    @Override
    public void traverse(ObjectMapperListener objectMapperListener)
    {      
    }
    
    @Override
    public void close()
    {
        try
        {
            if (_resultMapper != null) _resultMapper.close();
    
            reset();
        }
        catch (Throwable ex)
        {
            _logger.error("field mapper close", ex);
            if (ex instanceof RuntimeException) throw (RuntimeException)ex;
            else throw new RuntimeException(ex);
        }
    }
    
    public void reset()
    {
        if (_root != null)
        {
            _root.removeChild(this);
            _root = null;
        }
        
        _deleted = false;
    }
    
    public void execute(ParseContext context, Map<String, Object> vars) throws IOException
    {
        try
        {
            if (!enabled()) return;
    
            Object value =_scriptService.execute(_script, vars);
            if (value == null) return;

            if (_externalValueSupported)
            {
                context.externalValue(value);
                _resultMapper.parse(context);
            }
            else
            {
                FieldType fieldType = AbstractFieldMapper.Defaults.FIELD_TYPE;
                float boost = 1.0f;
                if (_resultMapper instanceof FieldMapper<?>) 
                {
                    FieldMapper<?> fm = (FieldMapper<?>)_resultMapper;
                    value = fm.value(value);
                    fieldType = fm.fieldType();
                    boost = fm.boost();
                }
                else if (_resultMapper instanceof GeoPointFieldMapper)
                {
                    GeoPointFieldMapper gfm = (GeoPointFieldMapper)_resultMapper;
                    
                    FieldMapper<?> fm = gfm.geoHashStringMapper();
                    if (fm == null) fm = gfm.latMapper();
                    
                    if (fm != null)
                    {
                        value = fm.value(value);
                        fieldType = fm.fieldType();
                        boost = fm.boost();             
                    }
                    else
                    {
                        fieldType = GeoPointFieldMapper.Defaults.FIELD_TYPE;              
                    }                   
                }
                
                ComputedField field = new ComputedField(_name, value, fieldType);
                field.setBoost(boost);
                context.doc().add(field);            
            }
        }
        catch (Throwable ex)
        {
            _logger.error("field mapper execute", ex);
            if (ex instanceof RuntimeException) throw (RuntimeException)ex;
            else throw new RuntimeException(ex);
        }
    }
    
    public void root(ComputedFieldRootMapper root)
    {
        _root = root;
    }
    
    public void deleted(boolean deleted)
    {
        _deleted = deleted;
    }
      
    public boolean enabled()
    {
        if (_root == null) return false;
        if (!_root.enabled()) return false;
        if (_resultMapper == null) return false;
        if (_script == null) return false;
        
        return !_deleted;
    }

    public static class TypeParser implements Mapper.TypeParser 
    {
        private final ScriptService _scriptService;
        
        public TypeParser(ScriptService scriptService)
        {
            _scriptService = scriptService;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public Mapper.Builder<?, ?> parse(String name, Map<String, Object> node, ParserContext parserContext) throws MapperParsingException
        {
            Builder builder = new Builder(name, _scriptService, node);

            Map<String, Object> result = (Map<String, Object>)node.get("result");
            if (result != null)
            {
                String type = XContentMapValues.nodeStringValue(result.get("type"), "string");
                builder.resultTypeParser(parserContext.typeParser(type).parse(name, result, parserContext));
            }  
                        
            return builder;
        }
        
    }
    
    public static class Builder extends Mapper.Builder<Builder, ComputedFieldMapper> 
    {
        private ScriptService _scriptService;
        private Mapper.Builder<?, ?>  _resultBuilder;
        private Map<String, Object> _mappings;
        
        protected Builder(String name, ScriptService scriptService, Map<String, Object> mappings)
        {
            super(name);
            
            _scriptService = scriptService;
            _mappings = mappings;
        }
        
        public Builder resultTypeParser(Mapper.Builder<?, ?> resultBuilder)
        {
            _resultBuilder = resultBuilder;
            return this;
        }
        
        @Override
        public ComputedFieldMapper build(BuilderContext context)
        {      
            return new ComputedFieldMapper(name, 
                    _scriptService,
                    _mappings,
                    _resultBuilder != null ? _resultBuilder.build(context) : null);
        }
    }
}
