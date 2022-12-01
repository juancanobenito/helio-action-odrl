package helio.action.odrl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import helio.blueprints.Action;
import helio.blueprints.exceptions.ActionException;
import odrl.lib.OdrlLib;
import odrl.lib.exceptions.OdrlRegistrationException;
import odrl.lib.operands.Time;

public class ODRL implements Action{

	private Logger logger = LoggerFactory.getLogger(ODRL.class);

	private OdrlLib odrl = new OdrlLib();
	
	protected static final Gson GSON = new Gson();
	protected static final Type empMapType = new TypeToken<Map<String, String>>() {}.getType();
	
	public ODRL() {
		odrl.registerPrefix("ops", "http://upm.es/operands#");
		try {
			odrl.register("ops", new Time());
		} catch (OdrlRegistrationException e) {
			logger.error(e.getMessage());
			throw new IllegalArgumentException(e.getMessage());
		}
		
	}
	
	public void configure(JsonObject configuration) {
		if(configuration.has("prefixes")) {
			JsonObject prefixesJson = configuration.get("prefixes").getAsJsonObject(); 
			Map<String, String>prefixes = GSON.fromJson(prefixesJson, empMapType);
			prefixes.entrySet().parallelStream().forEach(entry -> odrl.registerPrefix(entry.getKey(), entry.getValue()));

		}
	}

	public String run(String values) throws ActionException {
		JsonObject result = new JsonObject();
		try {
			JsonObject policy = GSON.fromJson(values, JsonObject.class);
			Map<String, List<String>> accessRights = odrl.solve(policy);
			if(!accessRights.isEmpty())
				accessRights.entrySet().forEach(entry -> result.add(entry.getKey(), toJsonArray(entry.getValue())));
			}catch(Exception e) {
				throw new ActionException(e.getMessage());
			}
		return result.toString();
	}

	private JsonArray toJsonArray(List<String> values) {
		JsonArray array = new JsonArray();
		values.parallelStream().forEach(elem -> array.add(elem));
		return array;
	}
}
