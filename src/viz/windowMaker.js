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
        .style("width", "75%")
        .style("border", "1px solid black");

    //var cols = table.append("colgroup");
    //cols.append("col").attr("span", "1").style("width", "60%");
    //cols.append("col").attr("span", "1").style("width", "30%");
    //cols.append("col").attr("span", "1").style("width", "10%");

    var headers = table.append("thead").append("tr");
    headers.style("border-bottom-style", "1px solid black");
    headers.append("th").html("Formula");
    headers.append("th").html("File");
    headers.append("th").html("Sheet and Cell");

    var rows = table.append("tbody").selectAll("tr")
        .data(childrenExamples)
        .enter()
        .append("tr");

    rows.each(function(d, i) {
        var row = d3.select(this);

        if (i % 2 == 0)
            row.style("background-color", "#ddd");

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

            row.append("td").style("width", "600px").style("word-wrap", "break-word").style("padding", "5px").html(formula.replace(/,/g, ", "));
            row.append("td").style("width", "200px").style("word-wrap", "break-word").style("padding", "5px").html(file);
            row.append("td").style("width", "100px").style("word-wrap", "break-word").style("padding", "5px").html(location);
        });
    });
}
