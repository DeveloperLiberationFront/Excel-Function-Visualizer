/**
 * Makes the tree.
 * https://bl.ocks.org/mbostock/4339184
 * https://bl.ocks.org/mbostock/4339083
 */

var margin = {
    top: 100,
    bottom: 100,
    left: 100,
    right: 100
  },
  height = 1000 - margin.left - margin.right,
  width = 5000 - margin.top - margin.bottom,
  duration = 500;

var scale; //To be determined when when tree chosen.

var tree = d3.layout.tree()
  .size([height, width])
  .sort(function(a, b) {
    return b.frequency - a.frequency;
  });

var svg = d3.select("body")
  .append("svg")
  .attr("width", width + margin.left + margin.right)
  .attr("height", height + margin.top + margin.bottom)
  .append("g")
  .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

var lines = d3.svg.line()
  .x(function(d) {
    return d.ly;
  })
  .y(function(d) {
    return d.lx;
  })
  .interpolate("linear");

d3.text('examples.php?id=1', function(error, data) {
  console.log(data);
})

var src = "sumtree.json"; //If python server started in tree folder
var root;
d3.json(src, function(error, json) {
  if (error) throw error;

  root = json;
  root.x0 = height / 2;
  root.y0 = 0;

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

  console.log(getDeepestLevel(root))

  nodes.forEach(function(d) {
    d.y = d.depth * 90;
  })

  scale = d3.scale.linear()
    .domain([0, nodes[0].frequency])
    .range([5, 20]);

  var node = svg.selectAll("g")
    .data(nodes, function(d) {
      return d.id || (d.id = ++i);
    });

  enterNode(node, src);
  updateNode(node, src);
  exitNode(node, src);

  var link = svg.selectAll("path.link")
    .data(links, function(d) {
      return d.target.id;
    });

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
      return "translate(" + src.y0 + "," + src.x0 + ")";
    })
    .on("click", click);

  if (areArguments(src)) {
    enterCircle(nodeEnter, src);
  } else {
    enterRect(nodeEnter, src);
  }
}
function enterCircle(nodeEnter, src) {
  nodeEnter.append("circle")
    .attr("r", 1e-6)
    .transition().duration(duration)
    .attr("r", function(d) {
      return scale(d.frequency);
    })
    .style("fill", function(d) {
      return d._children ? "#4C7B61" : "white";
    });

  nodeEnter.append("text")
    .attr("dx", function(d) {
      return d.children || d._children ? -8 : 8;
    })
    .attr("dy", 3)
    .attr("text-anchor", function(d) {
      return d.children || d._children ? "end" : "start";
    }).text(function(d) {
      return d.function;
    })
}
function enterRect(nodeEnter, src) {
  nodeEnter.append("rect")
    .attr("width", 20)
    .attr("height", 20)
    .attr("x", -10)
    .attr("y", -10)
    .style("fill", function(d) {
      return d._children ? "#627884" : "white";
    });

  nodeEnter.append("text")
    .text(function(d) {
      return d.position;
    });
}

function updateNode(node, src) {
  var nodeUpdate = node.transition()
    .duration(duration)
    .attr("transform", function(d) {
      return "translate(" + d.y + "," + d.x + ")";
    });

  if (areArguments(src)) {
    updateCircle(nodeUpdate, src);
  } else {
    updateRect(nodeUpdate, src);
  }

  nodeUpdate.select("text")
    .style("fill-opacity", 1);
}
function updateCircle(nodeUpdate, src) {
  nodeUpdate.select("circle")
    .attr("r", function(d) {
      return scale(d.frequency);
    })
    .style("fill", function(d) {
      return d._children ? "rgb(32, 114, 68)" : "white";
    });
}
function updateRect(nodeUpdate, src) {
  nodeUpdate.select("rect")
    .attr("width", 20)
    .attr("height", 20)
    .attr("x", -10)
    .attr("y", -10)
    .style("fill", function(d) {
      return d._children ? "#627884" : "white";
    });
}

function exitNode(node, src) {
  var nodeExit = node.exit().transition()
    .duration(duration)
    .attr("transform", function(d) {
      return "translate(" + src.y + "," + src.x + ")";
    })
    .remove()

  var i = 0;
  console.log(nodeExit);
  nodeExit.forEach(function(d) {
    ++i;
  });
  console.log("HEY " + i);

  if (areArguments(src)) {
    exitCircle(nodeExit, src);
  } else {
    exitRect(nodeExit, src);
  }

  nodeExit.select("text")
    .attr("fill-opacity", 1e-6);
}
function exitCircle(nodeExit) {
  nodeExit.select("circle")
    .attr("r", 1e-6)
}
function exitRect(nodeExit) {
  nodeExit.select("rect")
    .attr("width", 1e-6)
    .attr("height", 1e-6)
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
    .attr("d", function(d) {
      return getLine(d);
    });
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
function areArguments(d) {
  return d.depth % 2 == 0 ? false : true;
}
