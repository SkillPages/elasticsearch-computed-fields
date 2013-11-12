package org.elasticsearch.index.fields;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;

public final class ComputedField extends Field
{
    public ComputedField(String name, Object value, FieldType fieldType) 
    {
        super(name, fieldType);
        value(value);
    }
    
    private void value(Object value)
    {
        if (value instanceof Boolean) fieldsData = ((Boolean)value).booleanValue() ? "T" : "F";
        else if (value instanceof Number) fieldsData = (Number)value;
        else fieldsData = value.toString();
    }
}
