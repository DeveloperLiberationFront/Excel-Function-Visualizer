/**
 * Makes the tree.
 * https://bl.ocks.org/mbostock/4339184
 * https://bl.ocks.org/mbostock/4339083
 * http://bl.ocks.org/robschmuecker/6afc2ecb05b191359862
 * http://bl.ocks.org/mbostock/3680999
 */

var margin = {
    top: 100,
    bottom: 100,
    left: 100,
    right: 100
  },
  height = window.innerHeight - margin.top - margin.bottom - 20,
  width = window.innerWidth - margin.left - margin.right - 20,
  duration = 500,
  square_side = 20,
  square_side_half = square_side / 2,

  view_start_x = margin.left,
  view_start_y = height / 2 + margin.top;

var scale; //To be determined when when tree chosen.

/**
 * Create the tree and define it's general behavior.
 */
var tree = d3.layout.tree()
  //.size([height, width])
  .nodeSize([square_side, square_side])
  .sort(function(a, b) {
    var order = b.frequency - a.frequency;
    if (order == 0)
      if (a.function) order = a.function.localeCompare(b.function);
      else order = a.position - b.position;
    return order;
  });

/**
 * Define the zooming behavior for the svg element below this.
 */
var zoomer = d3.behavior.zoom()
  .scaleExtent([.5, 8])
  .on("zoom", function() {
    svg.attr("transform", "translate(" + d3.event.translate + ")scale("
      + d3.event.scale + ")")
  })

/**
 * Create the svg box in which you will see everything.
 */
var svg = d3.select("body")
  .append("svg")
  .attr("width", width + margin.left + margin.right)
  .attr("height", height + margin.top + margin.bottom)
  .append("g")
  .call(zoomer)
  .on("dblclick.zoom", null)
  .attr("transform", "translate(" + view_start_x + "," + view_start_y + ")")
  .append("g");

/**
 * Create a line generator, into which you must pass two x,y coordinates.
 */
var lines = d3.svg.line()
  .x(function(d) { return d.ly; })
  .y(function(d) { return d.lx; })
  .interpolate("linear");

/**
 * Initialize the tree with only one level down expanded.
 */
var src ="iftree.json"; //If python server started in tree folder
var root;
d3.json(src, function(error, json) {
  if (error) throw error;

  root = json;
  root.x0 = 0;
  root.y0 = 0;

  scale = d3.scale.log()
    .domain([1, root.frequency])
    .range([5, 15])
    .nice();

  function collapse(d) {
    if (d.children && d.children.length > 0) {
      d._children = d.children;
      d._children.forEach(collapse);
      d.children = null;
    } else {
      d.children = null;
      d._children = null;
    }
  }

  root.children.forEach(collapse);
  update(root);
})

d3.select(self.frameElement)
  .style("height", height + "px");

/**
 * Update the tree after a clicking event.
 */
var i = 0;
function update(src) {
  var nodes = tree.nodes(root),
    links = tree.links(nodes);

  nodes.forEach(function(d) { d.y = d.depth * 150; })

  var node = svg.selectAll("g")
    .data(nodes, function(d) { return d.id || (d.id = ++i); });

  enterNode(node, src);
  updateNode(node, src);
  exitNode(node, src);

  var link = svg.selectAll("path.link")
    .data(links, function(d) { return d.target.id; });

  enterLink(link, src);
  updateLink(link, src);
  exitLink(link, src);

  nodes.forEach(function(d) {
    d.x0 = d.x;
    d.y0 = d.y;
  });
}

/**
 * Creates both circular function nodes and rectangular function argument nodes.
 */
function enterNode(node, src) {
  var nodeEnter = node.enter()
    .append("g")
    .attr("class", "node")
    .attr("transform", function(d) {
      var functionNode = d.depth % 2 == 0,
        y = src.y0 + (functionNode ? 0 : square_side_half),
        x = src.x0 + (functionNode ? 0 : square_side_half);
      return "translate(" + y + "," + x + ")";
    })
    .on("click", click);

  var circles = nodeEnter.filter(function(d) { return d.depth % 2 == 0; }),
    rects = nodeEnter.filter(function(d) { return d.depth % 2 != 0; })

  enterCircle(circles, src);
  enterRect(rects, src);
}

/**
 * Creates the new function nodes.
 */
function enterCircle(circles, src) {
  circles.append("circle")
    .attr("r", 1e-6)
    .style("fill", function(d) { return d._children ? "#4C7B61" : "white"; })
    .on("mouseover", mouseover)
    .on("mousemove", mousemove)
    .on("mouseleave", mouseleave);

  circles.append("text")
    .attr("dx", -15 )
    .attr("dy", 3)
    .attr("text-anchor", "end")
    .text(function(d) { return d.function; })
}

/**
 * The tooltip that will appear over a moused-over function node.
 * Has function name, frequency, boilerplate, actual example.
 */
var tip = d3.select("body")
  .append("svg.rect")
  .attr("class", "tooltip")
  .attr("color", "gray")
  .style("display", "none");

/**
 * Makes the tooltip visible over current mouse position over node. Sets text.
 */
var tip_x = 3,
    tip_y = -53;
function mouseover(d) {
  var b = "<b>", bb = "</b>",
      i = "<i>", ii = "</i>",
      br = "<br/>",
      func = b + d.function + bb + br,
      freq = d.frequency.toLocaleString() + br,
      full = i + "unavailable" + ii + br,
      ex = i + "unavailable" + ii,

      id = d.example;

  d3.json("examples.php?id=" + id, function(error, data) {
    if (error) throw error;

    form = data["formula"].replace(/</g, "&lt;").replace(/>/g, "&gt;");
    ex = i + form + ii + br;
    tip.html(func + freq + full + ex)

    console.log(ex);
    //TODO: Changing widths of the tooltip?
    /*var width = $('.tooltip').width();
    console.log(width);
    if (width > 200) {
      tip.style("width", null);
    }*/
  })

  tip.html(func + freq + full + ex)
    .style("left", (d3.event.pageX + tip_x) + "px")
    .style("top", (d3.event.pageY + tip_y) + "px")
    .style("display", "inline");
}

/**
 * Moves the tooltip in relation to mouse pointer.
 */
function mousemove(d) {
  return tip.style("left", (d3.event.pageX + tip_x) + "px")
    .style("top", (d3.event.pageY + tip_y) + "px");
}

/**
 * Dismisses the tooltip once the mouse leaves the node.
 */
function mouseleave(d) {
  tip.style("display", "none");
}

/**
 * Create the rectangles which represent the argument
 * positions in a given function.
 */
function enterRect(rects, src) {
  //Starts rectangle infinitesimally small, so updateNode brings it to size.
  rects.append("rect")
    .attr("width", 1e-6)
    .attr("height", 1e-6)
    .attr("x", -square_side_half)
    .attr("y", -square_side_half)
    .style("fill", function(d) { return d._children ? "#627884" : "white"; });

  rects.append("text")
    .text(function(d) { return d.position + 1; })
    .attr("dx", function(d) { return d.position > 9 ? -7 : -3; })
    .attr("dy", 5);
}

/**
 * Transitions the new nodes to their intended position and shape.
 */
function updateNode(node, src) {
  var nodeUpdate = node.transition()
    .duration(duration)
    .attr("transform", function(d) {
      return "translate(" + d.y + "," + d.x + ")";
    });

  nodeUpdate.select("circle")
    .attr("r", function(d) { return scale(d.frequency); })
    .style("fill", function(d) {
      return d._children ? "rgb(32, 114, 68)" : "white";
    });

  nodeUpdate.select("rect")
    .attr("width", square_side)
    .attr("height", square_side)
    .style("fill", function(d) { return d._children ? "#627884" : "white"; });

  nodeUpdate.select("text")
    .style("fill-opacity", 1);
}

/**
 * Dismisses the nodes that will no longer be visible.
 */
function exitNode(node, src) {
  var nodeExit = node.exit().transition()
    .duration(duration)
    .attr("transform", function(d) {
      var functionNode = d.depth % 2 == 0,
        y = src.y + (functionNode ? 0 : square_side_half),
        x = src.x + (functionNode ? 0 : square_side_half);
      return "translate(" + y + "," + x + ")";    })
    .remove()

  nodeExit.select("circle")
    .attr("r", 1e-6);

  nodeExit.select("rect")
    .attr("width", 1e-6)
    .attr("height", 1e-6);

  nodeExit.select("text")
    .attr("fill-opacity", 1e-6);
}

/**
 * Creates links to new nodes.
 */
function enterLink(link, src) {
  link.enter()
    .insert("path", "g")
    .attr("class", "link")
    .attr("d", function(d) {
      var o = {
        x: src.x0,
        y: src.y0
      };

      return getLine({
        source: o,
        target: o
      });
    });
}

/**
 * Transitions the new lines to their intended position.
 */
function updateLink(link, src) {
  link.transition()
    .duration(duration)
    .attr("d", function(d) { return getLine(d); });
}

/**
 * Dismisses the links that are no longer necessary.
 */
function exitLink(link, src) {
  link.exit().transition()
    .duration(duration)
    .attr("d", function(d) {
      var o = {
        x: src.x,
        y: src.y
      };

      return getLine({
        source: o,
        target: o
      });
    })
    .remove();
}

/**
 * Generates a new line between two points.
 */
function getLine(d) {
  var coords = [{
    lx: d.source.x,
    ly: d.source.y
  }, {
    lx: d.target.x,
    ly: d.target.y
  }];

  return lines(coords);
}

/**
 * Expands or dismisses children nodes when a given node is clicked.
 */
function click(d) {
  if (d.children) {
    d._children = d.children;
    d.children = null;
  } else {
    d.children = d._children;
    d._children = null;
  }

  update(d);
}

/**
function getDeepestLevel(src) {
  var deepest = src.depth;

  if (src.children) {
    src.children.forEach(function(d) {
      var deepLevel = getDeepestLevel(d);
      deepest = deepLevel > deepest ? deepLevel : deepest;
    })
  }

  return deepest;
}
**I don't think this is necessary anymore.*/
