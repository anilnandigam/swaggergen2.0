package com.tmo.swagger.main;

import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.PropertyBuilder;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.SwaggerParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmo.swagger.exception.EmptyXlsRows;
import com.tmo.swagger.model.Parameters;
import com.tmo.swagger.model.Responses;
import com.tmo.swagger.model.RowData;
import com.tmo.swagger.util.PropertyReader;
import com.tmo.swagger.util.XlsReader;

public class GenrateSwaggerJson {
	final static Logger logger = Logger.getLogger(GenrateSwaggerJson.class);

	public static void main(String[] args) throws JsonGenerationException,
			JsonMappingException, IOException, EmptyXlsRows {

		PropertyReader pr=new PropertyReader();
		
		//Properties prop =pr.readPropertiesFile(args[0]);
		Properties prop =pr.readClassPathPropertyFile("common.properties");
		String swaggerFile=prop.getProperty("swagger.json");
		String sw="";
		if(swaggerFile!=null && swaggerFile.length() > 0){
			Swagger swagger=populatePropertiesOnlyPaths(prop,new SwaggerParser().read(swaggerFile));
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			 sw=mapper.writeValueAsString(swagger);
		}else{
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			Swagger swagger=populateProperties(prop);
			sw=mapper.writeValueAsString(swagger);
		}
		try {
			//File file = new File(args[1]+prop.getProperty("path.operation.tags")+".json");
			File file = new File("src/main/resources/"+prop.getProperty("path.operation.tags")+".json");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(sw);
			logger.info("Swagger Genration Done!");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Swagger populateProperties(Properties prop) throws EmptyXlsRows, JsonProcessingException {
		Swagger swagger = new Swagger();

		Info info = new Info();
		info.setTitle(prop.getProperty("swagger.info.title"));
		info.setVersion(prop.getProperty("swagger.info.version"));
		info.setDescription(prop.getProperty("swagger.info.description"));

		Contact contact = new Contact();
		contact.setEmail(prop.getProperty("swagger.contact.email"));
		contact.setName(prop.getProperty("swagger.contact.name"));
		contact.setUrl(prop.getProperty("swagger.contact.url"));
		info.setContact(contact);

		License license = new License();
		license.setName(prop.getProperty("swagger.license.name"));
		license.setUrl(prop.getProperty("swagger.license.url"));
		info.setLicense(license);
		swagger.setInfo(info);
		swagger.setHost(prop.getProperty("swagger.host"));
		swagger.setBasePath(prop.getProperty("swagger.basePath"));

		List<Scheme> schemes = new ArrayList<Scheme>();
		String schemeArray = prop.getProperty("swagger.schemes");
		for (String s : schemeArray.split(Pattern.quote("|"))) {
			Scheme sc = Scheme.valueOf(s);
			if (sc != null) {
				schemes.add(sc);
			}
		}
		swagger.setSchemes(schemes);
		swagger=populatePropertiesOnlyPaths(prop,swagger);
		return swagger;
	}
	
	public static Swagger populatePropertiesOnlyPaths(Properties prop,Swagger swagger) throws EmptyXlsRows {
		PropertyReader pr=new PropertyReader();
		Properties prop1 =pr.readClassPathPropertyFile("Description.properties");
		Properties prop2 =pr.readClassPathPropertyFile("min_max_pattern.properties");
		String xlsFile=prop.getProperty("xpath.xls");
		List<RowData> responseList = new ArrayList<RowData>();
		List<RowData> requestList= new ArrayList<RowData>();
		XlsReader xr=new XlsReader();
		PopulateSwaggerDefination psd = new PopulateSwaggerDefination();
		List<String> sheetList=new ArrayList<String>();
		Map<String, Path> paths=swagger.getPaths();
		if(paths==null){
		 paths=new HashMap<String, Path>();
		}
		Path path= new Path();
		Operation operation=new Operation();
		List<String> listTags = new ArrayList<String>();
		String tags = prop.getProperty("path.operation.tags");
			listTags.add(tags);
		operation.setTags(listTags);
		operation.setSummary(prop.getProperty("path.operation.summary"));
		operation.setDescription(prop.getProperty("path.operation.description"));
		operation.setOperationId(prop.getProperty("path.operation.operationId"));
		
		List<Parameter> parameters = new ArrayList<Parameter>();
		List<Parameters> paraList=getParametersFormProperties(prop,"");
		List<Parameters> headerList=getParametersFormProperties(prop,"request.header.");
		paraList.addAll(headerList);
		
		for(Parameters p:paraList){
			String in=p.getIn();
			if(!in.equalsIgnoreCase("body")){
			SerializableParameter parameter=getParameter(in);
			parameter.setName(p.getName());
			parameter.setDescription(p.getDescription());
			parameter.setRequired(p.getRequired());
			parameter.setType(p.getType());
			parameters.add(parameter);
			}else{
				BodyParameter bodyParameter=getBodyParameter(in);
				bodyParameter.setName(p.getIn());
				bodyParameter.setDescription(p.getDescription());
				bodyParameter.setRequired(p.getRequired());
				Model schema=new RefModel(p.getType());
				bodyParameter.setSchema(schema);
			    String	sheetName=p.getType();
			    sheetList.add(sheetName);
				requestList = xr.readExcel(xlsFile, sheetName);
				swagger=psd.populateDefinitons(requestList, prop1, prop2,swagger);
				parameters.add(bodyParameter);
			}
			
		}
		operation.setParameters(parameters);
		
		Path existingPath=paths.get(prop.getProperty("path.url"));
		if(existingPath!=null){
			existingPath.set(prop.getProperty("path.operation"), operation);
			paths.put(prop.getProperty("path.url"), existingPath);
		}else{
		path.set(prop.getProperty("path.operation"), operation);
		paths.put(prop.getProperty("path.url"), path);
		}
		swagger.setPaths(paths);
		Map<String, Response> responses=new HashMap<String, Response>();
		List<Responses> responsesList=getResponsesFormProperties(prop);
		
		for(Responses r:responsesList){
			Response response=new Response();
			response.setDescription(r.getDescription());
			if(r.getSchema()!=null && !r.getSchema().isEmpty()){
			RefProperty refProperty=new RefProperty();
			refProperty.set$ref(r.getSchema());
			response.setSchema(refProperty);
			String sheetName=r.getSchema();
			if(!sheetList.contains(sheetName)){
				sheetList.add(sheetName);
				responseList=xr.readExcel(xlsFile, r.getSchema());
				swagger=psd.populateDefinitons(responseList, prop1, prop2,swagger);
			}
			}
			if(r.getCode().equalsIgnoreCase("200") || r.getCode().equalsIgnoreCase("201") ){
				List<Parameters> list=getParametersFormProperties(prop,"response.header.");
				Map<String, Property> resHeaders=getParameterMap(list);
				response.setHeaders(resHeaders);
			}
			responses.put(r.getCode(), response);
		}
		operation.setResponses(responses);
		return swagger;
	}
	
	
	protected static  Map<String, Property> getParameterMap(List<Parameters> paraList){
		 Map<String, Property> headers=new HashMap<String, Property>();
		for(Parameters p:paraList){
			Property property=PropertyBuilder.build(p.getType(), null, null);
			property.setDescription(p.getDescription());
			property.setRequired(p.getRequired());
			headers.put(p.getName(),property);
			}
		return headers;
	}
	
	
	protected static SerializableParameter getParameter(String parameterObjectType){
		SerializableParameter parameter=null;
		if(parameterObjectType.equalsIgnoreCase("path")){
			parameter=new PathParameter();
		}else if(parameterObjectType.equalsIgnoreCase("query")){
			parameter=new QueryParameter();
		}else if(parameterObjectType.equalsIgnoreCase("header")){
			parameter=new HeaderParameter();
		}
		
		return parameter;
	}
	
	protected static BodyParameter getBodyParameter(String parameterObjectType){
		BodyParameter parameter=null;
		if(parameterObjectType.equalsIgnoreCase("body")){
			parameter=new BodyParameter();
		}
		
		return parameter;
	}
	
	protected static List<Parameters>  getParametersFormProperties(Properties prop,String initial){
		List<Parameters> parameters = new ArrayList<Parameters>();
		//
		Parameters parameter = null;
		String name = prop.getProperty(initial+"path.operation.parameters.name");
		if(name!=null && name.trim().length()>0){
		for (String s : name.split(Pattern.quote("|"))) {
			parameter = new Parameters();
			parameter.setName(s);
			parameters.add(parameter);
		}

		parameter = null;
		int count = 0;
		String in = prop.getProperty(initial+"path.operation.parameters.in");
		for (String s : in.split(Pattern.quote("|"))) {
			parameter = parameters.get(count);
			parameter.setIn(s);
			parameters.set(count, parameter);
			count++;
		}

		parameter = null;
		count = 0;
		String description = prop
				.getProperty(initial+"path.operation.parameters.description");
		for (String s : description.split(Pattern.quote("|"))) {
			parameter = parameters.get(count);
			parameter.setDescription(s);
			parameters.set(count, parameter);
			count++;
		}
		parameter = null;
		count = 0;
		String required = prop
				.getProperty(initial+"path.operation.parameters.required");
		for (String s : required.split(Pattern.quote("|"))) {
			parameter = parameters.get(count);
			Boolean boolean1 = Boolean.valueOf(s);
			parameter.setRequired(boolean1);
			parameters.set(count, parameter);
			count++;
		}
		parameter = null;
		count = 0;
		String type = prop.getProperty(initial+"path.operation.parameters.type");
		for (String s : type.split(Pattern.quote("|"))) {
			parameter = parameters.get(count);
			parameter.setType(s);
			parameters.set(count, parameter);
			count++;
		}
		/*parameter = null;
		count = 0;
		String maximum = prop.getProperty("path.operation.parameters.maximum");
		for (String s : maximum.split(",")) {
			parameter = parameters.get(count);
			parameter.setType(s);
			parameters.set(count, parameter);
			count++;
		}*/
		
		count = 0;
		}
		return parameters;
		
	}
	
	protected static List<Responses>  getResponsesFormProperties(Properties prop){
		List<Responses> responses = new ArrayList<Responses>();
		
		Responses response = null;
		String code = prop.getProperty("path.operation.responses.code");
		for (String s : code.split(Pattern.quote("|"))) {
			response = new Responses();
			response.setCode(s);
			responses.add(response);
		}

		response = null;
		int count = 0;
		String description = prop.getProperty("path.operation.responses.code.description");
		for (String s : description.split(Pattern.quote("|"))) {
			response = responses.get(count);
			response.setDescription(s);
			responses.set(count, response);
			count++;
		}

		response = null;
		count = 0;
		String schema = prop
				.getProperty("path.operation.responses.code.schema");
		for (String s : schema.split(Pattern.quote("|"))) {
			response = responses.get(count);
			response.setSchema(s);
			responses.set(count, response);
			count++;
		}
		response = null;
		count = 0;
		return responses;
	}
	
}
