package rundeck.controllers

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import rundeck.Option
import spock.lang.Specification

/**
 * Created by greg on 2/11/16.
 */
@TestFor(EditOptsController)
@Mock(Option)
class EditOptsControllerSpec extends Specification {
    def "validate opt required scheduled job with default storage path"() {
        given:
        Option opt = new Option(required: true, defaultValue: defval, defaultStoragePath: defstorageval)

        when:
        EditOptsController._validateOption(opt, params, true)
        then:
        iserr == opt.errors.hasFieldErrors('defaultValue')

        where:
        iserr | defval | defstorageval
        true  | null   | null
        false | 'abc'  | null
        false | null   | 'abc'

    }

    def "reorder option relative position"() {
        given:
        Option opt1 = new Option(name: 'abc')
        Option opt2 = new Option(name: 'def')
        Option opt3 = new Option(name: 'ghi')
        def opts = [opt1, opt2, opt3]
        def editopts = opts.collectEntries { [it.name, it] }

        when:
        def output = controller._applyOptionAction(
                editopts,
                [action: 'reorder', name: opt, params: [relativePosition: rel]]
        )

        then:
        editopts == [abc: opt1, def: opt2, ghi: opt3]
        output == [undo: [action: 'reorder', name: opt, params: [relativePosition: rel * -1]]]
        result == new TreeSet(opts)*.name

        where:
        opt   | rel | result
        'abc' | 1   | ['def', 'abc', 'ghi']
        'abc' | 2   | ['def', 'ghi', 'abc']
        'def' | -1  | ['def', 'abc', 'ghi']
        'ghi' | -2  | ['ghi', 'abc', 'def']
    }

    def "reorder option before"() {
        given:
        Option opt1 = new Option(name: 'abc')
        Option opt2 = new Option(name: 'def')
        Option opt3 = new Option(name: 'ghi')
        def opts = [opt1, opt2, opt3]
        def editopts = opts.collectEntries { [it.name, it] }

        when:
        def output = controller._applyOptionAction(
                editopts,
                [action: 'reorder', name: opt, params: [before: otherOpt]]
        )

        then:
        editopts == [abc: opt1, def: opt2, ghi: opt3]
        output == [undo: [action: 'reorder', name: opt, params: [relativePosition: undoPos]]]
        result == new TreeSet(opts)*.name

        where:
        opt   | otherOpt | undoPos | result
        'abc' | 'ghi'    | -1      | ['def', 'abc', 'ghi']
        'abc' | 'def'    | 0       | ['abc', 'def', 'ghi']
        'ghi' | 'abc'    | 2       | ['ghi', 'abc', 'def']
        'ghi' | 'def'    | 1       | ['abc', 'ghi', 'def']
        'def' | 'abc'    | 1       | ['def', 'abc', 'ghi']
    }

    def "reorder option last"() {
        given:
        Option opt1 = new Option(name: 'abc')
        Option opt2 = new Option(name: 'def')
        Option opt3 = new Option(name: 'ghi')
        def opts = [opt1, opt2, opt3]
        def editopts = opts.collectEntries { [it.name, it] }

        when:
        def output = controller._applyOptionAction(
                editopts,
                [action: 'reorder', name: opt, params: [last: true]]
        )

        then:
        editopts == [abc: opt1, def: opt2, ghi: opt3]
        output == [undo: [action: 'reorder', name: opt, params: [relativePosition: undoPosition]]]
        result == new TreeSet(opts)*.name

        where:
        opt   | undoPosition | result
        'abc' | -2           | ['def', 'ghi', 'abc']
        'def' | -1           | ['abc', 'ghi', 'def']
        'ghi' | 0            | ['abc', 'def', 'ghi']
    }


}
