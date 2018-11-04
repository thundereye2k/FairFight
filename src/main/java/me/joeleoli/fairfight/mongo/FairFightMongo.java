package me.joeleoli.fairfight.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.Collections;
import lombok.Getter;
import me.joeleoli.fairfight.FairFight;
import me.joeleoli.nucleus.config.ConfigCursor;
import org.bson.Document;

@Getter
public class FairFightMongo {

	private MongoClient client;
	private MongoDatabase database;
	private MongoCollection<Document> logs;

	public FairFightMongo() {
		ConfigCursor cursor = new ConfigCursor(FairFight.getInstance().getMainFileConfig(), "mongo");

		if (!cursor.exists("host")
		    || !cursor.exists("port")
		    || !cursor.exists("database")
		    || !cursor.exists("authentication.enabled")
		    || !cursor.exists("authentication.username")
		    || !cursor.exists("authentication.password")
		    || !cursor.exists("authentication.database")) {
			throw new RuntimeException("Missing configuration option");
		}

		if (cursor.getBoolean("authentication.enabled")) {
			final MongoCredential credential = MongoCredential.createCredential(
					cursor.getString("authentication.username"),
					cursor.getString("authentication.database"),
					cursor.getString("authentication.password").toCharArray()
			);

			this.client = new MongoClient(
					new ServerAddress(cursor.getString("host"), cursor.getInt("port")),
					Collections.singletonList(credential)
			);
		} else {
			this.client = new MongoClient(new ServerAddress(cursor.getString("host"), cursor.getInt("port")));
		}

		this.database = this.client.getDatabase("fairfight");
		this.logs = this.database.getCollection("alert_logs");
	}

	public static FairFightMongo getInstance() {
		return FairFight.getInstance().getMongo();
	}

}
