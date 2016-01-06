package com.tmo.swagger.main;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Swagger;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.PropertyBuilder;
import io.swagger.models.properties.PropertyBuilder.PropertyId;
import io.swagger.models.properties.RefProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.tmo.swagger.model.RowData;

public class PopulateSwaggerDefination {
	final static Logger logger = Logger
			.getLogger(PopulateSwaggerDefination.class);

	public static void main(String[] args) {

	}

	public Swagger populateDefinitons(List<RowData> rd, Properties p,
			Properties p1, Swagger swagger) {
		Map<String, Model> definitions = swagger.getDefinitions();
		if (definitions == null) {
			definitions = new HashMap<String, Model>();
		}
		for (RowData r : rd) {
			String xpath = r.getXpath();
			//System.out.println(xpath);
			String typ = r.getJsonType();
			String[] xpathArray = xpath.split("/");
			String lastBut = "";
			String root = null;
			String last = xpathArray[xpathArray.length - 1];
			
			if (xpathArray.length >= 2) {
				lastBut = xpathArray[xpathArray.length - 2];
			} else {
				lastBut = xpathArray[0];
				root = lastBut;
			}
			if (typ.equalsIgnoreCase("object")) {
				Model rootModel = null;
				if (root != null) {
					rootModel = definitions.get(root);
				}
				if (rootModel == null) {
					Model model = definitions.get(lastBut);
					if (model == null) {
						ModelImpl modelImpl = new ModelImpl();
						modelImpl.setType(ModelImpl.OBJECT);
						Model m=definitions.get(last);
						if(m==null){
						definitions.put(last, modelImpl);
						String des = p.getProperty(last);
						if (des != null && des.length() > 0) {
							modelImpl.setDescription(des);
						}
						}
					} else {
						if (model instanceof ModelImpl) {
							Map<String, Property> proMap = model
									.getProperties();
							if (proMap == null) {
								Map<String, Property> propertties = new HashMap<String, Property>();

								Property property = getModelProperty(r, p, last);
								propertties.put(last, property);
								model.setProperties(propertties);

							} else {

								Property property = getModelProperty(r, p, last);
								proMap.put(last, property);
								model.setProperties(proMap);
							}
							Model model1 = definitions.get(last);
							if (model1 == null) {
								ModelImpl modelImpl = new ModelImpl();
								modelImpl.setType(ModelImpl.OBJECT);
								definitions.put(last, modelImpl);
							}
						}
					}
				}
			} else {
				Model model = definitions.get(lastBut);
				if (model == null) {

				} else {
					Map<String, Property> proMap = model.getProperties();

					if (proMap == null) {
						Map<String, Property> propertties = new HashMap<String, Property>();
						Property property = getProperty(r, p, p1, lastBut, last);
						propertties.put(last, property);
						model.setProperties(propertties);

					} else {

						Property property = getProperty(r, p, p1, lastBut, last);
						proMap.put(last, property);

						model.setProperties(proMap);
					}
				}
				definitions.put(lastBut, model);
			}

		}
		swagger.setDefinitions(definitions);
		return swagger;
	}

	public static Property getProperty(RowData r, Properties p, Properties p1,
			String lastBut, String last) {
		Map<PropertyBuilder.PropertyId, Object> map = new HashMap<PropertyBuilder.PropertyId, Object>();
		String mmp = p1.getProperty(lastBut);
		if (mmp != null && mmp.length() > 0) {
			String[] a = mmp.split(Pattern.quote("|"));
			int c = 0;
			for (String s : a) {
				if (c == 0) {
					map.put(PropertyId.MIN_LENGTH, s);
				} else if (c == 1) {
					map.put(PropertyId.MAX_LENGTH, s);
				} else if (c == 2) {
					map.put(PropertyId.PATTERN, s);
				}
				c++;
			}

		}
		List<String> l = r.getEnumcell();
		if (l != null && l.size() > 0) {
			map.put(PropertyId.ENUM, l);
		}
		String des = p.getProperty(last);
		if (des != null && des.length() > 0) {
			map.put(PropertyId.DESCRIPTION, des);
		}
		if (r.getMax().equals("*")) {
			if (r.getMin().equalsIgnoreCase("1")) {
				map.put(PropertyId.MIN_ITEMS, 1);
			}

			map.put(PropertyId.TYPE, "array");
			ArrayProperty ar = (ArrayProperty) PropertyBuilder.build("array",
					null, map);
			Property property = PropertyBuilder.build(r.getJsonType(),
					(r.getJsonFormat() != null && r.getJsonFormat().trim()
							.length() > 0) ? r.getJsonFormat().trim() : null, map);
			ar.setItems(property);
			return ar;
		} else {
			Property property = PropertyBuilder.build(r.getJsonType(),
					(r.getJsonFormat() != null && r.getJsonFormat().trim()
							.length() > 0) ? r.getJsonFormat().trim() : null, map);
			if (r.getMin().equals("1") && r.getMax().equals("1")) {
				//System.out.println(last);
				property.setRequired(true);
			}
			return property;
		}

	}

	public static Property getModelProperty(RowData r, Properties p, String last) {
		Map<PropertyBuilder.PropertyId, Object> map = new HashMap<PropertyBuilder.PropertyId, Object>();
		if (r.getMax().equals("*")) {

			String des = p.getProperty(last);
			if (des != null && des.length() > 0) {
				map.put(PropertyId.DESCRIPTION, des);
			}
			if (r.getMin().equalsIgnoreCase("1")) {
				map.put(PropertyId.MIN_ITEMS, 1);
				map.put(PropertyId.MAX_ITEMS, 2147483647);
			}
			ArrayProperty ar = (ArrayProperty) PropertyBuilder.build("array",
					null, map);
			RefProperty refProperty = new RefProperty();
			refProperty.set$ref(last);
			ar.setItems(refProperty);
			return ar;
		} else {
			RefProperty property = (RefProperty) PropertyBuilder.build("ref",
					null, map);
			property.set$ref(last);
			return property;
		}
	}

}
