package org.elasticsearch.index.fields;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.lucene.all.AllEntries;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.mapper.ContentPath;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.index.mapper.object.RootObjectMapper;

public class ParseContextWrapper extends ParseContext
{
    private ParseContext _parseContext;
    private XContentParser _parser;

    public ParseContextWrapper(final ParseContext parseContext)
    {
        super(null, null, null, null, null);
        _parseContext = parseContext;
    }

    public void reset(final Object value) throws IOException
    {
        if (value instanceof String)
        {
            String strVal = (String) value;

            if (!strVal.startsWith("{") && !strVal.endsWith("}"))
            {
                strVal = "[\"" + strVal + "\"]";
                _parser = JsonXContent.jsonXContent.createParser(strVal);
                _parser.nextToken();
            }
            else _parser = JsonXContent.jsonXContent.createParser(strVal);
        }
        else
        {
            XContentBuilder json = XContentFactory.jsonBuilder();
            json.startArray();
            json.value(value);
            json.endArray();
            
            _parser = JsonXContent.jsonXContent.createParser(json.bytes());
            _parser.nextToken();
        }
        
        _parser.nextToken();
    }

    @Override
    public XContentParser parser()
    {
        if (externalValueSet()) return _parseContext.parser();
        else return _parser;
    }

    public boolean flyweight()
    {
        return _parseContext.flyweight();
    }

    public DocumentMapperParser docMapperParser()
    {
        return _parseContext.docMapperParser();
    }

    public boolean mappingsModified()
    {
        return _parseContext.mappingsModified();
    }

    public void setMappingsModified()
    {
        _parseContext.mappingsModified();
    }

    public void setWithinNewMapper()
    {
        _parseContext.setWithinNewMapper();
    }

    public void clearWithinNewMapper()
    {
        _parseContext.clearWithinNewMapper();
    }

    public boolean isWithinNewMapper()
    {
        return _parseContext.isWithinNewMapper();
    }

    public String index()
    {
        return _parseContext.index();
    }

    @Nullable
    public Settings indexSettings()
    {
        return _parseContext.indexSettings();
    }

    public String type()
    {
        return _parseContext.type();
    }

    public SourceToParse sourceToParse()
    {
        return _parseContext.sourceToParse();
    }

    public BytesReference source()
    {
        return _parseContext.source();
    }

    // only should be used by SourceFieldMapper to update with a compressed
    // source
    public void source(BytesReference source)
    {
        _parseContext.source(source);
    }

    public ContentPath path()
    {
        return _parseContext.path();
    }

    @SuppressWarnings("rawtypes")
    public DocumentMapper.ParseListener listener()
    {
        return _parseContext.listener();
    }

    public Document rootDoc()
    {
        return _parseContext.rootDoc();
    }

    public List<Document> docs()
    {
        return _parseContext.docs();
    }

    public Document doc()
    {
        return _parseContext. doc();
    }

    public void addDoc(Document doc)
    {
        _parseContext.addDoc(doc);
    }

    public Document switchDoc(Document doc)
    {
        return _parseContext.switchDoc(doc);
    }

    public RootObjectMapper root()
    {
        return _parseContext.root();
    }

    public DocumentMapper docMapper()
    {
        return _parseContext.docMapper();
    }

    public AnalysisService analysisService()
    {
        return _parseContext.analysisService();
    }

    public String id()
    {
        return _parseContext.id();
    }

    public void ignoredValue(String indexName, String value)
    {
        _parseContext.ignoredValue(indexName, value);
    }

    public String ignoredValue(String indexName)
    {
        return _parseContext.ignoredValue(indexName);
    }

    public void id(String id)
    {
        _parseContext.id(id);
    }

    public Field uid()
    {
        return _parseContext.uid();
    }

    public void uid(Field uid)
    {
        _parseContext.uid(uid);
    }

    public Field version()
    {
        return _parseContext.version();
    }

    public void version(Field version)
    {
        _parseContext.version(version);
    }

    @SuppressWarnings("rawtypes")
    public boolean includeInAll(Boolean includeInAll, FieldMapper mapper)
    {
        return _parseContext.includeInAll(includeInAll, mapper);
    }

    public AllEntries allEntries()
    {
        return _parseContext.allEntries();
    }

    public Analyzer analyzer()
    {
        return _parseContext.analyzer();
    }

    public void analyzer(Analyzer analyzer)
    {
        _parseContext.analyzer(analyzer);
    }

    public void externalValue(Object externalValue)
    {
        _parseContext.externalValue(externalValue);
    }

    public boolean externalValueSet()
    {
        return _parseContext.externalValueSet();
    }

    public Object externalValue()
    {
        return _parseContext.externalValue();
    }

    public float docBoost()
    {
        return _parseContext.docBoost();
    }

    public void docBoost(float docBoost)
    {
        _parseContext.docBoost(docBoost);
    }

    public StringBuilder stringBuilder()
    {
        return _parseContext.stringBuilder();
    }

}
