package org.elasticsearch.index.fields;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;

public final class ScriptParametersWrapper implements Map<String, Object>
{
    private final Document _doc;
    private final DocumentMapper _mapper;
    
    public ScriptParametersWrapper(Document doc, DocumentMapper mapper)
    {
        _doc = doc;
        _mapper = mapper;
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
        if (_doc == null) return null;
        if (_mapper == null) return null;
        if (key == null) return null;
        
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
            else if (mapper instanceof GeoPointFieldMapper)
            {
                GeoPointFieldMapper gfm = (GeoPointFieldMapper)mapper;
                
                FieldMapper<?> fm = gfm.stringMapper();
                if (fm == null) fm = gfm.geoHashStringMapper();
                
                if (fm != null) value = fm.value(value);
            }
        }
               
        return value;
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

}
