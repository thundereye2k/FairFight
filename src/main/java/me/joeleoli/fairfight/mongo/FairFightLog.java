package me.joeleoli.fairfight.mongo;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class FairFightLog {

    @Getter
    private static List<FairFightLog> queue = new ArrayList<>();

    private UUID uuid;
    private String flag;
    private String client;
    private int ping;
    private double tps;
    private long timestamp;

}
