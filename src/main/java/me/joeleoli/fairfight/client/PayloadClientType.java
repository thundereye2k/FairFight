package me.joeleoli.fairfight.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PayloadClientType implements ClientType {

    private final String name;
    private final String payload;
    private final boolean hacked;

}
