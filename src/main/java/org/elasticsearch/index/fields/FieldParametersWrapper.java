package org.elasticsearch.index.fields;


import org.apache.lucene.document.Document;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.object.ObjectMapper;

public final class FieldParametersWrapper extends BaseScriptParametersWrapper
{
    private final BaseScriptParametersWrapper _parent;
    private final ObjectMapper _mapper;
     
    public FieldParametersWrapper(final BaseScriptParametersWrapper parent, final ObjectMapper mapper)
    {
        _parent = parent;
        _mapper = mapper;
    }
    
    @Override
    protected Document doc()
    {
        return _parent.doc();
    }
    
    @Override
    protected DocumentMapper mapper()
    {
        return _parent.mapper();
    }   
    
    @Override
    protected boolean returnFieldData()
    {
        return _parent.returnFieldData();
    }
    
    @Override
    protected String path()
    {
        return _mapper.fullPath();
    }   
    
    
    @Override
    public Object get(final Object key)
    {
        if (key == null) return null;
        
        try
        {
            String path = _mapper.fullPath() + "." + (String)key;
            ObjectMapper obj = mapper().objectMappers().get(path);
            if (obj != null)
            {
                return new FieldParametersWrapper(this, obj);
            }
            else
            {
                return super.get(key);            
            }
        }
        catch (RuntimeException ex)
        {
            throw ex;
        }
    }
}
