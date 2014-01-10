Computed fields plugin for ElasticSearch 
===========================================

The computed fields plugin adds support for indexing computed(scripted) fields

In order to install the plugin, simply run: 
```
bin/plugin -i computed-fields -u https://github.com/SkillPages/elasticsearch-computed-fields/releases/download/v0.0.1/elasticsearch-computed-fields-0.0.1.zip
```

<table>
	<thead>
		<tr>
			<td>Computed Fields Plugin</td>
			<td>ElasticSearch</td>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>master</td>
			<td>master</td>
		</tr>
		<tr>
			<td>0.0.2</td>
			<td>0.90.9</td>
		</tr>
		<tr>
			<td>0.0.1</td>
			<td>0.90.7</td>
		</tr>
	</tbody>
</table>

### Examples:
&nbsp;
##### CREATE INDEX:
```javascript
curl -XPOST localhost:9200/twitter -d '{
    "mappings" : {
        "tweet" : {
            "computed_fields" : { "enabled" : true }
        }
    }
}'
```
```javascript
curl -XPOST localhost:9200/twitter -d '{
    "mappings" : {
        "tweet" : {
            "computed_fields" : { "enabled" : true },
            "properties" : {
                "latitude" : { "type" : "float" },
                "longitude" : { "type" : "float" }
            }
        }
    }
}'
```
```javascript
curl -XPOST localhost:9200/twitter -d '{
    "mappings" : {
        "tweet" : {
            "computed_fields" : { "enabled" : true },
            "properties" : {
                "latitude" : { "type" : "float" },
                "longitude" : { "type" : "float" },
                "@point" : { 
                    "type" : "computed", 
                    "script" : "latitude + ',' + longitude", 
                    "result" : {
                        "type" : "geo_point",
                        "store" : true
                    }
                }
            }
        }
    }
}'
```
IMPORTANT: Computed fields can not be enabled using put mapping request (they can only be enabled during index creation). This issue is caused by current limitation of elasticsearch, that dont allows adding of root field mappers during merge.

&nbsp;
##### PUT MAPPING:

```javascript
curl -XPUT localhost:9200/twitter/tweet/_mapping -d '{
    "tweet" : {
        "computed_fields" : { "enabled" : false },
        "properties" : {
            "latitude" : { "type" : "float" },
            "longitude" : { "type" : "double" },
            "@point" : { 
                "type" : "computed", 
                "script" : "longitude + ',' + latitude", 
                "result" : {
                    "type" : "geo_point",
                    "store" : true
                }
            }
        }
    }
}'
```
```javascript
curl -XPUT localhost:9200/twitter/tweet/_mapping -d '{
    "tweet" : {
        "computed_fields" : { "enabled" : true },
        "properties" : {
            "latitude" : { "type" : "float" },
            "longitude" : { "type" : "float" },
            "@point" : { 
                "type" : "computed", 
                "script" : "latitude + ',' + longitude", 
                "result" : {
                    "type" : "geo_point",
                    "store" : true
                }
            },
            "list" : 
            [
            	{
            	    "id" : 1 
            	},
            	{
            	    "id" : 2
            	}
            ]
            "@test" : { "type" : "computed", "script" : "doc['list.id'].values[1].toString() + doc['list.id'].value.toString() + list.id.toString() + _source.list[1].id.toString()", "result" : { "type" : "string" } }
        }
    }
}'
```
&nbsp;
##### INDEX:
```javascript
curl -XPUT localhost:9200/twitter/tweet/1 -d '{
    "latitude" : 53.3,
    "longitude" : -6.4
}'
```
```javascript
curl -XPUT localhost:9200/twitter/tweet/2 -d '{
    "latitude" : 53.3,
    "longitude" : -6.4,
    "text" : "test"
}'
```
&nbsp;
##### SEARCH:
```javascript
curl -XPOST localhost:9200/twitter/tweet/_search?pretty=true&fields=_source,@point -d '{
    "filter" : {
            "geo_bounding_box" : {
                "@point" : {
                    "top_left" : {
                        "lat" : 54.00,
                        "lon" : -9.00
                    },
                    "bottom_right" : {
                        "lat" : 50.00,
                        "lon" : -5.00
                    }
                }
            }
        }
}'
```
NOTE: Stored computed fields may not be returned immediatelly after indexing them, it is caused by issue in elasticsearch, that it is trying to extract stored fields from _source field when document is still 'warm' in transaction log. After transaction log gets flushed, computed stored fields are returned correctly. To manually flush transaction log, issue index optimize request (test only):
```javascript
curl -XPOST localhost:9200/twitter/_optimize
```


