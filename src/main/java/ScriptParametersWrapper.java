

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.Mapper;

public final class ScriptParametersWrapper implements Map<String, Object>
{
    private final Document _doc;
    private final DocumentMapper _mapper;
    private final ScriptParametersWrapper _inner;
    
    public ScriptParametersWrapper(Document doc, DocumentMapper mapper)
    {
        _doc = doc;
        _mapper = mapper;
        _inner = null;
    }
    
    private ScriptParametersWrapper(final ScriptParametersWrapper inner)
    {
        _doc = inner._doc;
        _mapper = inner._mapper;
        _inner = inner;
    }
    
    @Override
    public void clear()
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean containsKey(Object key)
    {
        return true;
    }

    @Override
    public boolean containsValue(Object arg0)
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet()
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Object get(Object key)
    {
        if (key == null) return null;

        if ("doc".equals(key)) return new ScriptParametersWrapper(this);

        if (_doc == null) return null;
        if (_mapper == null) return null;
        
        if (_inner != null)
        {
            IndexableField[] fields = _doc.getFields((String)key);
            if ((fields == null) || (fields.length == 0)) return null;
    
            FieldMapper<?> fm = null;
            Mapper mapper = _mapper.mappers().smartNameFieldMapper((String)key);
            if (mapper != null)
            {
                if (mapper instanceof FieldMapper<?>)
                {
                    fm = (FieldMapper<?>)mapper;
                }
            }
        
            FieldData value = new FieldData();
            value.values = new Object[fields.length];
            for (int i = 0; i < fields.length; i++)
            {
                value.values[i] = fields[i].numericValue();
                if (value.values[i] == null) value.values[i] = fields[i].stringValue();
               
                if (fm != null) value.values[i] = fm.value(value.values[i]);
            }
            value.value = value.values[0];
            return value;
        }
        else
        {
            IndexableField field = _doc.getField((String)key);
            if (field == null) return null;
            
            Object value = field.numericValue();
            if (value == null) value = field.stringValue();
    
            Mapper mapper = _mapper.mappers().smartNameFieldMapper((String)key);
            if (mapper != null)
            {
                if (mapper instanceof FieldMapper<?>)
                {
                    FieldMapper<?> fm = (FieldMapper<?>)mapper;
                    value = fm.value(value);
                }
            }
                   
            return value;
        }
    }

    @Override
    public boolean isEmpty()
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Set<String> keySet()
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Object put(String arg0, Object arg1)
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> arg0)
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Object remove(Object arg0)
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int size()
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Collection<Object> values()
    {
        throw new RuntimeException("not implemented");
    }

    public class FieldData
    {
        public Object[] values;
        public Object value;
    }
}
