package org.elasticsearch.index.fields;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.index.mapper.*;

public class ComputedFieldRootMapper implements RootMapper
{   
    private final static Map<Integer, ComputedFieldRootMapper> RootMappers = new HashMap<Integer, ComputedFieldRootMapper>();
    
    private final String _name;
    
    private Map<String, Object> _mappings;   
    private boolean _enabled;
    private List<ComputedFieldMapper> _children;
    private Integer _contentBuilderHash;
    
    protected ComputedFieldRootMapper(String name, Map<String, Object> mappings)
    {
        _name = name;
        _mappings = mappings;
        
        _enabled = XContentMapValues.nodeBooleanValue(mappings.get("enabled"), false);
    }
    
    @Override
    public String name()
    {
        return _name;
    }
    
    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException
    {
        if (_children == null)
        {
            synchronized(RootMappers)
            {
                if (_children == null)
                {                
                    _contentBuilderHash = System.identityHashCode(builder);
                    RootMappers.put(_contentBuilderHash, this);
                    _children = new ArrayList<ComputedFieldMapper>();
                }
            }
        }
        
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
        ComputedFieldRootMapper m = (ComputedFieldRootMapper)mergeWith;
        if (!mergeContext.mergeFlags().simulate())
        {
            _mappings = m._mappings;
            _enabled = m._enabled;
            
            if (_children != null)
            {
                for (ComputedFieldMapper child : _children)
                {
                    boolean deleted = true;
                    if (m._children != null)
                    {
                        for (ComputedFieldMapper child2 : m._children)
                        {
                            if (child.name() == child2.name())
                            {
                                deleted = false;
                                break;
                            }
                        }
                    }
                    
                    child.deleted(deleted);
                }
            }
            
            reset();
        }
        m.close();
    }

    @Override
    public void traverse(FieldMapperListener fieldMapperListener)
    {
    }

    @Override
    public void traverse(ObjectMapperListener objectMapperListener)
    {      
    }
    
    @Override
    public void close()
    {
        reset();
    }

    @Override
    public void preParse(ParseContext context) throws IOException
    {
    }

    @Override
    public void postParse(ParseContext context) throws IOException
    {
        if (!_enabled) return;
        if (_children == null) return;
        
        Map<String, Object> vars = new ScriptParametersWrapper(context.doc(), context.docMapper());        
        for (ComputedFieldMapper child : _children)
        {
            child.execute(context, vars);
        }
    }

    @Override
    public void validate(ParseContext context) throws MapperParsingException
    {
    }

    @Override
    public boolean includeInObject()
    {
        return false;
    }
    
    public boolean enabled()
    {
        return _enabled && (_contentBuilderHash != null);
    }
    
    public void addChild(ComputedFieldMapper computedFieldMapper)
    {
        if (_children == null)
            _children = new ArrayList<ComputedFieldMapper>();
        
        _children.add(computedFieldMapper);
        computedFieldMapper.root(this);
    }
    
    public void removeChild(ComputedFieldMapper computedFieldMapper)
    {
        if (_children == null) return;
        
        _children.remove(computedFieldMapper);
        computedFieldMapper.root(null);
        
        if (_children.size() == 0) reset();
    }
    
    public void reset()
    {
        if (_contentBuilderHash != null)
        {
            synchronized(RootMappers)
            {
                if (_contentBuilderHash != null)
                {
                    RootMappers.remove(_contentBuilderHash);
                    _contentBuilderHash = null;
                    _children = null;
                }
            }
        }       
    }
    
    public static void Register(XContentBuilder builder, ComputedFieldMapper computedFieldMapper)
    {
        if (builder == null) return;
        
        Integer hash = System.identityHashCode(builder);
        
        ComputedFieldRootMapper root = RootMappers.get(hash);
        if (root == null) return;
        
        root.addChild(computedFieldMapper);
    }
    
    public static class TypeParser implements Mapper.TypeParser 
    {       
        public TypeParser()
        {
        }
        
        @Override
        public Mapper.Builder<?, ?> parse(String name, Map<String, Object> node, ParserContext parserContext) throws MapperParsingException
        {
            Builder builder = new Builder(name, node);                       
            return builder;
        }    
    }
    
    public static class Builder extends Mapper.Builder<Builder, ComputedFieldRootMapper> 
    {
        private Map<String, Object> _mappings;
        
        protected Builder(String name, Map<String, Object> mappings)
        {
            super(name);
            
            _mappings = mappings;
        }
        
        @Override
        public ComputedFieldRootMapper build(BuilderContext context)
        {      
            return new ComputedFieldRootMapper(name, 
                    _mappings); 
        }
    }
}
