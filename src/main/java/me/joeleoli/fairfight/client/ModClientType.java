package me.joeleoli.fairfight.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ModClientType implements ClientType {

    private final String name;
    private final String modId;
    private final String modVersion;
    
    @Override
    public boolean isHacked() {
        return true;
    }

}
