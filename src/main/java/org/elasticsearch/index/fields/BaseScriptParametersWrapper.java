package org.elasticsearch.index.fields;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.IndexableField;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.FieldMappers;
import org.elasticsearch.index.mapper.ParseContext.Document;

public abstract class BaseScriptParametersWrapper implements Map<String, Object>
{
    protected abstract Document doc();   
    protected abstract DocumentMapper mapper();
    protected abstract boolean returnFieldData();
    protected abstract String path();
    
    @Override
    public Object get(Object key)
    {
        String path = path();
        if (path == null) path = (String)key;
        else path = path + "." + (String)key;
        
        if (returnFieldData())
        {
            IndexableField[] fields = doc().getFields(path);
            if ((fields == null) || (fields.length == 0)) return null;
    
            FieldMapper<?> fm = null;
            
            FieldMappers mappers = mapper().mappers().fullName(path);
            if ((mappers != null) && !mappers.isEmpty())
            {
                fm = mappers.mapper();
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
            IndexableField field = doc().getField(path);
            if (field == null) return null;
            
            Object value = field.numericValue();
            if (value == null) value = field.stringValue();
    
            FieldMappers mappers = mapper().mappers().fullName(path);
            if ((mappers != null) && !mappers.isEmpty())
            {
                FieldMapper<?> mapper = mappers.mapper();
                mapper.value(value);
            }
                   
            return value;
        }
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
