/**
 * Makes the tree.
 * https://bl.ocks.org/mbostock/4339184
 * https://bl.ocks.org/mbostock/4339083
 * http://bl.ocks.org/robschmuecker/6afc2ecb05b191359862
 * http://bl.ocks.org/mbostock/3680999
 * http://bl.ocks.org/robschmuecker/7880033
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

  view_start_x = width / 2,
  view_start_y = height / 2,

  circle_col =    "#4C7B61",
  circle_hover =  "#1D5134",
  rect_col =      "#627884",
  rect_hover =    "#35425B",
  expand_col =    "#ADAD6B",
  expand_hover =  "#898949"
  empty_col =     "white",
  empty_hover =   "lightgray";

var scale; //To be determined when when tree chosen.

var diagonal = d3.svg.diagonal()
  .projection(function(d) { return [d.y, d.x]; });

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
  .call(zoomer)
  .on("dblclick.zoom", null)
  .append("g")
  .attr("transform", "translate(" + view_start_x + "," + view_start_y + ")");

zoomer.translate([view_start_x, view_start_y]);

//gist.github.com/pnavarrc/20950640812489f13246
var gradient = svg.append("defs")
  .append("linearGradient")
  .attr("id", "gradient");
  /*.attr("x1", "0")
  .attr("y1", "1")
  .attr("x2", "1")
  .attr("y2", "1");*/

gradient.append("stop")
  .attr("offset", "0")
  .attr("stop-color", "black")
  .attr("stop-opacity", "1");

gradient.append("stop")
  .attr("offset", "1")
  .attr("stop-color", "black")
  .attr("stop-opacity", "0");

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
    .range([3, 20])
    .nice();

  function collapse(d) {
    if (d.children && d.children.length > 0) {
      d._children = d.children;
      d._children.forEach(collapse);
      d.children = null;

      if (d.position != null && d._children.length > 10) {
        d._children.sort(function(a,b) { return b.frequency - a.frequency; });
        d._holding = d._children.splice(10);
        d._children.splice(d._children.length, 0,
          {"function":"", "frequency":-1, "parent":this});
      }
    } else {
      d.children = null;
      d._children = null;
    }
  }

  root.children.forEach(collapse);
  center(root);
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
    .attr("id", function(d) { return "n" + d.id; })
    .attr("transform", function(d) {
      var functionNode = d.depth % 2 == 0,
        y = src.y0 + (functionNode ? 0 : square_side_half),
        x = src.x0 + (functionNode ? 0 : square_side_half);
      return "translate(" + y + "," + x + ")";
    });

  var circles = nodeEnter.filter(function(d) {
        return d.depth % 2 == 0 && d.frequency > 0;
      }),
      expand = nodeEnter.filter(function(d) {
        return d.depth % 2 == 0 && d.frequency == -1;
      }),
      rects = nodeEnter.filter(function(d) { return d.depth % 2 != 0; })

  circles.filter(function(d) { return d.children || d._children; }) //Only click those that have any children.
    .on("click", click);
  rects.on("click", click)
    .on("mouseover", rect_mouseover)
    .on("mouseleave", rect_mouseout);
  expand.on("click", expandclick);

  enterCircle(circles);
  enterRect(rects);

  expand.append("polygon")
    .attr("height", "20")
    .attr("width", "20")
    .attr("points", "0,0 0,0 0,0")
    //.attr("points", "-10,-5 0,15 10,-5")
    .style("fill", expand_col)
    .on("mouseover", function(d) { d3.select("#n" + d.id).select("polygon")
      .style("fill", expand_hover); })
    .on("mouseleave", function(d) { d3.select("#n" + d.id).select("polygon")
      .style("fill", expand_col); })
}

/**
 * Creates the new function nodes.
 */
function enterCircle(circles) {
  circles.append("circle")
    .attr("r", 1e-6)
    .style("fill", function(d) { return d._children ? circle_col : empty_col; })
    .on("mouseover", mouseover)
    .on("mousemove", mousemove)
    .on("mouseleave", mouseleave);

  circles.append("text")
    .attr("dx", function(d) { return -scale(d.frequency) - 2; })
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

  var hovered = d3.select("#n" + d.id)
    .select("circle");

  hovered.style("fill", function(d) {
    if (d._children) return circle_hover;     //Get darker color.
    else if (d.children) return empty_hover;  //Get gray.
    else return empty_col;                    //Don't change if no children.
  });
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

  var hovered = d3.select("#n" + d.id)
    .select("circle");

  hovered.style("fill", function(d) {
    if (d._children) return circle_col;
    else return empty_col;
  });
}

/**
 * Create the rectangles which represent the argument
 * positions in a given function.
 */
function enterRect(rects) {
  //Starts rectangle infinitesimally small, so updateNode brings it to size.
  rects.append("rect")
    .attr("width", 1e-6)
    .attr("height", 1e-6)
    .attr("x", -square_side_half)
    .attr("y", -square_side_half)
    .style("fill", function(d) { return d._children ? rect_col : empty_col; });

  rects.append("text")
    .text(function(d) { return d.position + 1; })
    .attr("dx", function(d) { return d.position + 1 > 9 ? -7 : -3; })
    .attr("dy", 5);
}

function rect_mouseover(d) {
  d3.select("#n" + d.id).select("rect").style("fill", function(d) {
    if (d._children) return rect_hover;
    else return empty_hover;
  });
}

function rect_mouseout(d) {
  d3.select("#n" + d.id).select("rect").style("fill", function(d) {
    if (d._children) return rect_col;
    else return empty_col;
  });
}

/**
 * Transitions the new nodes to their intended position and shape.
 */
function updateNode(node) {
  var nodeUpdate = node.transition()
    .duration(duration)
    .attr("transform", function(d) {
      return "translate(" + d.y + "," + d.x + ")";
    });

  nodeUpdate.select("circle")
    .attr("r", function(d) { return scale(d.frequency); })
    .style("fill", function(d) {
      return d._children ? circle_col : empty_col;
    });

  nodeUpdate.select("rect")
    .attr("width", square_side)
    .attr("height", square_side)
    .style("fill", function(d) { return d._children ? rect_col : empty_col; });

  nodeUpdate.select("polygon")
  .attr("height", "20")
  .attr("width", "20")
  .attr("points", function(d) {
    if (d.parent._holding)
      return "-10,-5 0,15 10,-5";
    else
      return "-10,15 0,-5 10,15";
  })
  .style("fill", expand_col);

  nodeUpdate.select("text")
    .style("fill-opacity", 1);

  nodeUpdate.attr("stroke-width", function(d) {
    if (!d._children && d.frequency > 0) {
      return "1px";
    } else
      return "0px";
  })
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

  nodeExit.select("polygon")
    .attr("width", 1e-6)
    .attr("height", 1e-6)
    .attr("points", "0,0 0,0 0,0");

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
      var o = { x: src.x0, y: src.y0 };
      return diagonal({ source: o, target: o });
    });
}

/**
 * Transitions the new lines to their intended position.
 */
function updateLink(link, src) {
  link.transition()
    .duration(duration)
    .attr("d", diagonal);
}

/**
 * Dismisses the links that are no longer necessary.
 */
function exitLink(link, src) {
  link.exit().transition()
    .duration(duration)
    .attr("d", function(d) {
      var o = { x: src.x, y: src.y };
      return diagonal({ source: o, target: o });
    })
    .remove();
}

/**
 * Expands or dismisses children nodes when a given node is clicked.
 */
function click(d) {
  //Circles just have toggle functionality: click, and see all.
  if (d.children) {
    d._children = d.children;
    d.children = null;
  } else {
    d.children = d._children;
    d._children = null;
  }

  center(d);
  update(d);
}

function expandclick(d) {
  var par = d.parent;

  if (par._holding) {
    par.children = par.children.concat(par._holding);
    par._holding = null;
  } else {
    par._holding = par.children.splice(10)
      .filter(function(i) { return i.frequency != -1; });
    par.children.splice(par.children.length, 0, d);
  }

  center(par);
  update(par);
}

//http://bl.ocks.org/robschmuecker/7880033
function center(d) {
  var scale = zoomer.scale(),
      x = -d.y0 * scale + (width/2),
      y = -d.x0 * scale + (height/2);
  d3.select("g").transition().duration(duration)
    .attr("transform", "translate(" + x + "," + y + ")scale(" + scale + ")");
  zoomer.scale(scale);
  zoomer.translate([x, y]);
}