var WindowMaker = {};
WindowMaker.makeWindow = newWindow;

/**
 * Opens a new window and shows all of the examples available for it.
 */
function newWindow(d) {
    if (d3.event.shiftKey || d3.event.ctrlKey)
        return; //TODO: A bigger problem is limiting the number of examples.

    var win = window.open("", "_blank");
    win.document.write("You clicked: " + d.function + "<br/><br/>")
    win.focus;

    var childrenExamples = {};

    function getAllExamples(node) {
        if (node.allExamples) {
            node.allExamples.forEach(function(d) {
                childrenExamples[d] = true;
            });
        } else {
            if (node.children) node.children.forEach(getAllExamples);
            if (node._children) node._children.forEach(getAllExamples);
            if (node._holding) node._holding.forEach(getAllExamples);
        }
    }

    getAllExamples(d);

    //For resource purposes, select only the first 1000.
    childrenExamples = Object.keys(childrenExamples).slice(0, 1000);

    var table = d3.select(win.document.body)
        .append("table")
        .style("width", "75%");

    var cols = table.append("colgroup");
    cols.append("col").attr("span", "1").style("width", "60%");
    cols.append("col").attr("span", "1").style("width", "30%");
    cols.append("col").attr("span", "1").style("width", "10%");

    var rows = table.selectAll("tr")
        .data(childrenExamples)
        .enter()
        .append("tr");

    rows.each(function(d, i) {
        var row = d3.select(this);

        if (i % 2 == 0)
            row.attr("color", "#eee");

        d3.json("examples.php?id=" + d, function(error, data) {
            var formula, file, location;
            if (error) {
                formula = "unavailable";
                file = "unavailable";
                location = "unavailable";
            } else {
                formula = data["formula"].replace(/</g, "&lt;").replace(/>/g, "&gt;");
                location = "'" + data["sheetName"] + "'!" + data["col"] + (parseInt(data["row"], 10) + 1);
                file = data["file"];
                file = "<a href=\"localhost:8000/sheets/ENRON/" + file + "#" + location + "\">" + file + "</a>";
            }

            row.append("td").html(formula);
            row.append("td").html(file);
            row.append("td").html(location);
        });
    });
}
