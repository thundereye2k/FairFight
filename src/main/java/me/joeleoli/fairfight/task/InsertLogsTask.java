package me.joeleoli.fairfight.task;

import me.joeleoli.fairfight.mongo.FairFightLog;
import me.joeleoli.fairfight.mongo.FairFightMongo;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InsertLogsTask implements Runnable {

    @Override
    public void run() {
        final List<Document> documents = new ArrayList<>();
        final Iterator<FairFightLog> iterator = FairFightLog.getQueue().iterator();

        while (iterator.hasNext()) {
            final FairFightLog log = iterator.next();
            final Document document = new Document();

            document.put("uuid", log.getUuid().toString());
            document.put("flag", log.getFlag());
            document.put("client", log.getClient());
            document.put("ping", log.getPing());
            document.put("tps", log.getTps());
            document.put("timestamp", log.getTimestamp());

            documents.add(document);

            iterator.remove();
        }

        if (!documents.isEmpty()) {
            FairFightMongo.getInstance().getLogs().insertMany(documents);
        }
    }

}
