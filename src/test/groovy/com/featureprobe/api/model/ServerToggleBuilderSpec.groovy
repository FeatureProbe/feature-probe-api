package com.featureprobe.api.model

import spock.lang.Specification


class ServerToggleBuilderSpec extends Specification {

    def "build boolean server toggles"() {
        when:
        def toggle = new ServerToggleBuilder().builder().key("toggle1")
                .disabled(true)
                .forClient(true)
                .returnType("boolean")
                .version(1000)
                .rules(new TargetingContent(
                        rules: [newSelectRule("rule1", 0, [newStringCondition("city", ["1"])])],
                        disabledServe: newSelectServe(1),
                        defaultServe: newSplitServe([2000, 3000, 5000]),
                        variations: newVariations(["true", "false"])).toJson()).build()
        then:
        with(toggle) {
            "toggle1" == key
            !enabled
            forClient
            1000 == version
            1 == rules.size()
            [[[0, 2000]], [[2000, 5000]], [[5000, 10000]]] == defaultServe.split.distribution
            1 == disabledServe.select
            [true, false] == variations

            with(rules[0]) {
                1 == conditions.size()
            }
        }
    }

    def "build number server toggles"() {
        when:
        def toggle = new ServerToggleBuilder().builder().key("toggle1")
                .returnType("number")
                .version(1000)
                .rules(new TargetingContent(
                        rules: [],
                        disabledServe: newSelectServe(1),
                        defaultServe: newSplitServe([5000, 5000]),
                        variations: newVariations(["1.14", "1000"])).toJson()).build()
        then:
        with(toggle) {
            "toggle1" == key
            1000 == version
            [[[0, 5000]], [[5000, 10000]]] == defaultServe.split.distribution
            0 == rules.size()
            [1.14, 1000] == variations
        }
    }

    def "build server toggles when default serve is null should be use disabled serve"() {
        when:
        def toggle = new ServerToggleBuilder().builder().key("toggle1")
                .returnType("boolean")
                .version(1000)
                .disabled(true)
                .rules(new TargetingContent(
                        rules: [],
                        defaultServe: new ServeValue(),
                        disabledServe: newSelectServe(1),
                        variations: newVariations(["true", "false"])).toJson()).build()
        then:
        with(toggle) {
            0 == rules.size()
            defaultServe != null
            1 == defaultServe.select
        }
    }



    def newSelectRule(name, select, conditions) {
        new ToggleRule(name: name, serve: new ServeValue(select: select), conditions: conditions)
    }

    def newSelectServe(select) {
        new ServeValue(select: select)
    }

    def newSplitServe(splits) {
        new ServeValue(split: splits)
    }


    def newVariations(values) {
        values.collect { v -> new Variation(value: v) }

    }

    def newStringCondition(subject, objects) {
        new ConditionValue(type: "string", subject: subject, predicate: "is one of", objects: objects)
    }
}
