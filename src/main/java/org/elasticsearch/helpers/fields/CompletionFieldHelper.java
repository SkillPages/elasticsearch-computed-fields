package org.elasticsearch.helpers.fields;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentParser.NumberType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.mapper.MapperException;
import org.elasticsearch.index.mapper.core.CompletionFieldMapper;
import org.elasticsearch.index.mapper.core.CompletionFieldMapper.Fields;

import com.google.common.collect.Lists;

public final class CompletionFieldHelper
{
    private static final BytesRef EMPTY = new BytesRef();

    public static void parse(String value, final CompletionFieldMapper mapper, final Document doc) throws IOException
    {
        XContentParser parser = null;
        if (!value.startsWith("{") && !value.endsWith("}"))
        {
            value = "[\"" + value + "\"]";
            parser = JsonXContent.jsonXContent.createParser(value);
            parser.nextToken();
        }
        else parser = JsonXContent.jsonXContent.createParser(value);

        parser.nextToken();
        XContentParser.Token token = parser.currentToken();

        String surfaceForm = null;
        BytesRef payload = null;
        long weight = -1;
        List<String> inputs = Lists.newArrayListWithExpectedSize(4);

        if (token == XContentParser.Token.VALUE_STRING)
        {
            inputs.add(parser.text());
        }
        else
        {
            String currentFieldName = null;
            while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT)
            {
                if (token == XContentParser.Token.FIELD_NAME)
                {
                    currentFieldName = parser.currentName();
                    if (!CompletionFieldMapper.ALLOWED_CONTENT_FIELD_NAMES.contains(currentFieldName))
                    {
                        throw new ElasticSearchIllegalArgumentException("Unknown field name[" + currentFieldName + "], must be one of "
                                + CompletionFieldMapper.ALLOWED_CONTENT_FIELD_NAMES);
                    }
                }
                else if (Fields.CONTENT_FIELD_NAME_PAYLOAD.equals(currentFieldName))
                {
                    if (!mapper.isStoringPayloads())
                    {
                        throw new MapperException("Payloads disabled in mapping");
                    }
                    if (token == XContentParser.Token.START_OBJECT)
                    {
                        XContentBuilder payloadBuilder = XContentFactory.contentBuilder(parser.contentType()).copyCurrentStructure(parser);
                        payload = payloadBuilder.bytes().toBytesRef();
                        payloadBuilder.close();
                    }
                    else if (token.isValue())
                    {
                        payload = parser.bytesOrNull();
                    }
                    else
                    {
                        throw new MapperException("payload doesn't support type " + token);
                    }
                }
                else if (token == XContentParser.Token.VALUE_STRING)
                {
                    if (Fields.CONTENT_FIELD_NAME_OUTPUT.equals(currentFieldName))
                    {
                        surfaceForm = parser.text();
                    }
                    if (Fields.CONTENT_FIELD_NAME_INPUT.equals(currentFieldName))
                    {
                        inputs.add(parser.text());
                    }
                }
                else if (token == XContentParser.Token.VALUE_NUMBER)
                {
                    if (Fields.CONTENT_FIELD_NAME_WEIGHT.equals(currentFieldName))
                    {
                        NumberType numberType = parser.numberType();
                        if (NumberType.LONG != numberType && NumberType.INT != numberType)
                        {
                            throw new ElasticSearchIllegalArgumentException("Weight must be an integer, but was [" + parser.numberValue()
                                    + "]");
                        }
                        weight = parser.longValue(); // always parse a long to
                                                     // make sure we don't get
                                                     // the overflow value
                        if (weight < 0 || weight > Integer.MAX_VALUE)
                        {
                            throw new ElasticSearchIllegalArgumentException("Weight must be in the interval [0..2147483647], but was ["
                                    + weight + "]");
                        }
                    }
                }
                else if (token == XContentParser.Token.START_ARRAY)
                {
                    if (Fields.CONTENT_FIELD_NAME_INPUT.equals(currentFieldName))
                    {
                        while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY)
                        {
                            inputs.add(parser.text());
                        }
                    }
                }
            }
        }
        payload = payload == null ? EMPTY : payload;
        if (surfaceForm == null)
        {   // no surface form use the input
            for (String input : inputs)
            {
                BytesRef suggestPayload = mapper.buildPayload(new BytesRef(input), weight, payload);
                doc.add(mapper.getCompletionField(input, suggestPayload));
            }
        }
        else
        {
            BytesRef suggestPayload = mapper.buildPayload(new BytesRef(surfaceForm), weight, payload);
            for (String input : inputs)
            {
                doc.add(mapper.getCompletionField(input, suggestPayload));
            }
        }
        parser.close();
    }
}
