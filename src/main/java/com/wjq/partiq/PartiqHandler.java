package com.wjq.partiq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.protocol.ResponseServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.internal.http.LowCopyListMap.ForBuildable;
import software.amazon.awssdk.regions.Region;
//import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementResponse;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementRequest;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.xspec.IfNotExistsFunction;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PartiqHandler implements RequestStreamHandler {
	private String DYNAMO_TABLE = "ComponentTable";
	List<AttributeValue> parameters = new ArrayList<>();
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	String sqlStatement;
	ExecuteStatementResponse response;
	@SuppressWarnings("unchecked")
	
	public void handlePartiQRequest(InputStream input, OutputStream output, Context context) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(output);
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		JSONParser parser = new JSONParser(); // this will help us parse the request object
		JSONObject responseObject = new JSONObject(); // we will add to this object for our api response
		JSONObject responseBody = new JSONObject();// we will add the item to this object
		

        DynamoDbClient client = DynamoDbClient.builder()
        		.build();
        List<AttributeValue> parameters = new ArrayList<>();
        AttributeValue att1 = AttributeValue.builder()
            .s(DYNAMO_TABLE)
            .build();
        
        String sqlStatement;
        parameters.add(att1);
        Component component; 
        List list = new ArrayList<Component>();
        try {
        	
			JSONObject reqObject = (JSONObject) parser.parse(reader);

        	
        	if (reqObject.get("queryStringParameters")!=null) {
				JSONObject qps =(JSONObject) reqObject.get("queryStringParameters");
				if (qps.get("query")!=null) {
					sqlStatement = (String)qps.get("query");
					response = executeStatementRequest(client, sqlStatement);
					if(response!=null) {
					for (Map<String, AttributeValue> attrValue : response.items()) {
						component = new Component();
						for (String key : attrValue.keySet()) {
							
							if(key.contains("name")) {
								
								component.setName(attrValue.get(key).getValueForField("S", String.class).orElse("-999"));
								}
							if(key.contains("id")) {
								//attrValue.get(key).getValueForField("N", String.class).orElse(null);
								
								component.setId(Integer.parseInt(attrValue.get(key).getValueForField("N", String.class).orElse("-9991")));
								}
							if(key.contains("description"))component.setDescription(attrValue.get(key).getValueForField("S", String.class).orElse("-999"));
						
						}
						list.add(component);
						
					}
					}
				
				 }
				}
            if (response!=null) {
				//Component product = new Component(resItem.toJSON());
				responseBody.put("component", gson.toJson(list));
				responseObject.put("statusCode", 200);
			}else {
				responseBody.put("message", "No Items Found");
				responseObject.put("statusCode", 404);
			}
            
            responseObject.put("body", responseBody.toString());
            
        } catch (DynamoDbException e) {
        	context.getLogger().log("ERROR : "+e.getMessage());
        }catch(Exception e) {
        	context.getLogger().log("ERROR : "+e.getMessage());
        }
        writer.write(responseObject.toString());
		reader.close();
		writer.close();
	}
	//private  ExecuteStatementResponse executeStatementRequest(DynamoDbClient ddb, String statement, List<AttributeValue> parameters ) {

	@SuppressWarnings("unchecked")
	public void handlePartiQInsert(InputStream input, OutputStream output, Context context) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(output);
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		JSONParser parser = new JSONParser(); // this will help us parse the request object
		JSONObject responseObject = new JSONObject(); // we will add to this object for our api response
		JSONObject responseBody = new JSONObject();// we will add the item to this object
        DynamoDbClient client = DynamoDbClient.builder().build();
       
        String sqlStatement="";
      
        
        try {
        	
			JSONObject reqObject = (JSONObject) parser.parse(reader);

        	
        	if (reqObject.get("body")!=null) {
        		Sqlstatement bodyObj  = new Sqlstatement((String)reqObject.get("body"));
				
					sqlStatement = (String)bodyObj.getInsertSQL();
					response = executeStatementRequest(client, sqlStatement);
					
				 
				}
			
             

            if (response!=null) {
				//Component product = new Component(resItem.toJSON());
				responseBody.put("component", sqlStatement);
				responseObject.put("statusCode", 200);
			}else {
				responseBody.put("message", "No Items Found");
				responseObject.put("statusCode", 404);
			}
            
            responseObject.put("body", responseBody.toString());
            
        } catch (DynamoDbException e) {
        	context.getLogger().log("ERROR : "+e.getMessage());
        }catch(Exception e) {
        	context.getLogger().log("ERROR : "+e.getMessage());
        }
        writer.write(responseObject.toString());
		reader.close();
		writer.close();
	}

	private  ExecuteStatementResponse executeStatementRequest(DynamoDbClient ddb, String statement) {
        ExecuteStatementRequest request = ExecuteStatementRequest.builder()
                .statement(statement)
                //.parameters(parameters)
                
                .build();

        return ddb.executeStatement(request);
    }


	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
