package org.elasticsearch.index.fields;

import org.apache.lucene.index.IndexableField;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.ParseContext.Document;
import org.elasticsearch.index.mapper.object.ObjectMapper;

public final class DocumentParametersWrapper extends BaseScriptParametersWrapper
{
    private final Document _doc;
    private final DocumentMapper _mapper;
    private final DocumentParametersWrapper _inner;
    
    public DocumentParametersWrapper(Document doc, DocumentMapper mapper)
    {
        _doc = doc;
        _mapper = mapper;
        _inner = null;
    }
    
    private DocumentParametersWrapper(final DocumentParametersWrapper inner)
    {
        _doc = inner._doc;
        _mapper = inner._mapper;
        _inner = inner;
    }
    
    @Override
    protected Document doc()
    {
        return _doc;
    }
    
    @Override
    protected DocumentMapper mapper()
    {
        return _mapper;
    }
    
    @Override
    protected boolean returnFieldData()
    {
        return _inner != null;
    }
    
    @Override
    protected String path()
    {
        return null;
    }   

    @Override
    public Object get(Object key)
    {
        if (key == null) return null;
        if (_doc == null) return null;
        if (_mapper == null) return null;
       
        try
        {
            ObjectMapper obj = _mapper.objectMappers().get(key);
            if (obj != null)
            {
                return new FieldParametersWrapper(this, obj);
            }
            else
            {
                if ("doc".equals(key)) return new DocumentParametersWrapper(this);
                if ("_source".equals(key))
                {
                    IndexableField field = doc().getField((String)key);
                    if (field == null) return null;
                    
                    return XContentHelper.convertToMap(new BytesArray(field.binaryValue()), false).v2();
                }
                    
                return super.get(key);
            }
        }
        catch (RuntimeException ex)
        {
            throw ex;
        }
    }
}
