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

var tree = d3.layout.tree()
  //.size([height, width])
  .nodeSize([square_side, square_side])
  .sort(function(a, b) { return b.frequency - a.frequency; });

var zoomer = d3.behavior.zoom()
  .scaleExtent([.5, 8])
  .on("zoom", function() {
    svg.attr("transform", "translate(" + d3.event.translate + ")scale("
      + d3.event.scale + ")")
  })

var svg = d3.select("body")
  .append("svg")
  .attr("width", width + margin.left + margin.right)
  .attr("height", height + margin.top + margin.bottom)
  .append("g")
  .call(zoomer)
  .on("dblclick.zoom", null)
  .attr("transform", "translate(" + view_start_x + "," + view_start_y + ")")
  .append("g");

var lines = d3.svg.line()
  .x(function(d) { return d.ly; })
  .y(function(d) { return d.lx; })
  .interpolate("linear");

d3.text('examples.php?id=1', function(error, data) {
  console.log(data);
})

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

var i = 0;

function update(src) {
  var nodes = tree.nodes(root),
    links = tree.links(nodes);

  nodes.forEach(function(d) { d.y = d.depth * 100; })

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

function enterCircle(circles, src) {
  circles.append("circle")
    .attr("r", 1e-6)
    .style("fill", function(d) { return d._children ? "#4C7B61" : "white"; });

  circles.append("text")
    .attr("dx", function(d) { return d.children || d._children ? -15 : 10; })
    .attr("dy", 3)
    .attr("text-anchor", function(d) {
      return d.children || d._children ? "end" : "start";
    }).text(function(d) { return d.function; })
}

function enterRect(rects, src) {
  //Starts rectangle infinitesimally small, so updateNode brings it to size.
  rects.append("rect")
    .attr("width", 1e-6)
    .attr("height", 1e-6)
    .attr("x", -square_side_half)
    .attr("y", -square_side_half)
    .style("fill", function(d) { return d._children ? "#627884" : "white"; });

  rects.append("text")
    .text(function(d) { return d.position; })
    .attr("dx", function(d) { return d.position > 9 ? -7 : -3; })
    .attr("dy", 5);
}

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

function updateLink(link, src) {
  link.transition()
    .duration(duration)
    .attr("d", function(d) { return getLine(d); });
}

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
