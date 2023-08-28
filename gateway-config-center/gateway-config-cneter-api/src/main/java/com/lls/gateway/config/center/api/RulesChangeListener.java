package com.lls.gateway.config.center.api;

import com.lls.common.config.Rule;

import java.util.List;

public interface RulesChangeListener {
    void onRulesChange(List<Rule> rules);
}
