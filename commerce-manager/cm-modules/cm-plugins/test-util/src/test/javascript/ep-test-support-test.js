describe("EPTest Test Suite", function () {

    rwt =  {
            remote: {
                WidgetManager: " "
            }
        };

    var dummyElement = document.createElement("div");
    beforeEach(function () {
        document.getElementById = jasmine.createSpy("HTML Element").and.returnValue(dummyElement);
    });

    it("EPTest is defined", function () {
        expect(EPTest).toBeDefined();
    });

    describe("scrollToTableItemWithText", function() {
        var rootItem, cachedChildren, widget;

        beforeEach(function () {
            //setup test

            widget = {
                getRootItem: function() {
                    return rootItem;
                },
                scrollItemIntoView: function(a) {

                },
                 selectItem: function(a) {

                },
                 deselectAll: function(a) {
                },
                _columns: {
                    "columnId": {
                        _text : "columnName",
                        _index : 0
                        }
                },

                _rwtId: "mockId"
            };
            dummyElement.rwtWidget = widget;
            var manager = {
                widget:widget,
                findWidgetById:  function (id) {
                    return this.widget;
                }
            };
            rootItem = {
                getCachedChildren: function () {
                    return cachedChildren;
                }
            };
            spyOn(rwt.remote, "WidgetManager").and.returnValue(manager);
            spyOn(widget, 'scrollItemIntoView');
            spyOn(widget, 'deselectAll');
            spyOn(widget, 'selectItem');
        });

        it("returns true when item with string is found", function () {
            cachedChildren = [{
                _texts : ["testString"]
            }];

            var result = EPTest.scrollToTableItemWithText("dummy selector", "testString", "columnName");
            expect(result).toBe(true);
            expect(widget.scrollItemIntoView).toHaveBeenCalled();
            expect(widget.deselectAll).toHaveBeenCalled();
            expect(widget.selectItem).toHaveBeenCalled();

        });
        it("returns true when item with string with encoding markers is found", function () {
            var beginningMarker = '\uFEFF';
            var endMarker = '\u180E'
            cachedChildren = [{
                _texts : [beginningMarker + "testCode" + endMarker + "testString"]
            }];
            EPTest.setEncodingMarkers(beginningMarker, endMarker)

            var result = EPTest.scrollToTableItemWithText("dummy selector", "testString", "columnName");
            expect(result).toBe(true);
            expect(widget.scrollItemIntoView).toHaveBeenCalled();
            expect(widget.deselectAll).toHaveBeenCalled();
            expect(widget.selectItem).toHaveBeenCalled();

        });

        it("returns false when string is not found", function () {
            cachedChildren = [{
                _texts : ["testString"]
            }];

            var result = EPTest.scrollToTableItemWithText("dummy selector", "unused-string", "columnName");
            expect(result).toBe(false);
            expect(widget.scrollItemIntoView).not.toHaveBeenCalled();

        });
        it("returns false when string is in the wrong column", function () {
            cachedChildren = [{
                _texts : ["testString"]
            }];

            var result = EPTest.scrollToTableItemWithText("dummy selector", "testString", "otherColumnName");
            expect(result).toBe(false);
            expect(widget.scrollItemIntoView).not.toHaveBeenCalled();

        });
        it(" returns true when string is found in 2nd object", function () {
            cachedChildren = [
                {
                    _texts : ["testString" ]
                },
                {
                    _texts : ["second-string"]
                }];

            var result = EPTest.scrollToTableItemWithText("dummy selector", "second-string", "columnName");
            expect(result).toBe(true);
            expect(widget.scrollItemIntoView).toHaveBeenCalled();
            expect(widget.deselectAll).toHaveBeenCalled();
            expect(widget.selectItem).toHaveBeenCalled();

        });
    });

    describe("isButtonEnabled", function() {



        it("should return true if button is enabled", function() {
            var element = document.createElement("div");
            element.rwtWidget = {
                isEnabled: jasmine.createSpy("isEnabled").and.returnValue(true)
            };
            document.querySelector = jasmine.createSpy("enabledElement").and.returnValue(element);

            var result = EPTest.isButtonEnabled("test");
            expect(result).toBe(true);
        });

        it("should return null if button isn't found by the selector", function() {
            document.querySelector = jasmine.createSpy("enabledElement").and.returnValue(undefined);

            var result = EPTest.isButtonEnabled("test");
            expect(result).toBe(null);
        });
        it("should return null if element isn't a button", function() {
            var element = document.createElement("div");
            document.querySelector = jasmine.createSpy("enabledElement").and.returnValue(element);
            element.rwtWidget = {};

            var result = EPTest.isButtonEnabled("test");
            expect(result).toBe(null);
        });
    });


});

