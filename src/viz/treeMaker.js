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

    circle_empty = "#C0D6C2",
    circle_col = "#6DBF7E",
    circle_hover = "#5E966E",

    rect_empty = "#ABBABA",
    rect_col = "#519393",
    rect_hover = "#284E5E",

    alternate_rect = "#B1BEC4",

    expand_col = "#CFE5A7",
    expand_hover = "#BBCE96",
    empty_col = "white",
    empty_hover = "lightgray";

d3.select(self.frameElement)
    .style("height", height + "px");

var scale; //To be determined when when tree chosen.

var diagonal = d3.svg.diagonal()
    .projection(function(d) {
        return [d.y, d.x];
    });

/**
 * Create the tree and define it's general behavior.
 */
var tree = d3.layout.tree()
    //.size([height, width])
    .nodeSize([square_side, square_side])
    .sort(function(a, b) {
        var order = 0;

        if (isFunction(a) && isFunction(b)) {
          order = (b.frequency - a.frequency)
            || a.function.localeCompare(b.function);
        } else if (isPosition(a) && isPosition(b)) {
          order = a.position - b.position;
        }

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
zoomer.translate([view_start_x, view_start_y]);

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
      .attr("transform", "translate(" + view_start_x + ","
        + view_start_y + ")");

//gist.github.com/pnavarrc/20950640812489f13246
var interfunc_gap = 125,
    func_arg_gap = 75,  //func_arg_gap >= 2*interfunc_gap
    gradient = svg.append("defs")
      .append("linearGradient")
      .attr("id", "gradient")
      .attr("gradientUnits", "userSpaceOnUse")
      .attr("spreadMethod", "repeat")
      .attr("x1", "0px")
      .attr("y1", "0px")
      .attr("x2", (interfunc_gap*2).toString() + "px")
      .attr("y2", "0px");
gradient.append("stop")
    .attr("offset", ".4")
    .attr("stop-color", "black")
    .attr("stop-opacity", ".75");
gradient.append("stop")
    .attr("offset", ".8")
    .attr("stop-color", "black")
    .attr("stop-opacity", "0");

//https://css-tricks.com/snippets/javascript/get-url-variables/
function getQueryVariable(variable) {
    var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split("=");
        if (pair[0] == variable) {
            return pair[1];
        }
    }

    return false;
}

/**
 * Initialize the tree with only one level down expanded.
 */
var src = "smalljson/j" + getQueryVariable("file") + ".json"; //If python server started in tree folder
var root;
d3.json(src, function(error, json) {
    if (error) throw error;

    root = json;
    root.x0 = 0;
    root.y0 = 0;
    root.max_depth = 0;

    scale = d3.scale.log()
        .domain([1, root.frequency])
        .range([5, 25])
        .nice();

    startCollapsing(root);
    initQuantities(root);
    toggleChildren(root);

    center(root);
    update(root);
})

function startCollapsing(parent) {
  parent.max_depth = 0;

  if (parent.children && parent.children.length > 0) {
    parent.children.forEach(function(child) {
      var possible_max = startCollapsing(child) + (isFunction(parent) ? 0 : 1);
      parent.max_depth = Math.max(possible_max, parent.max_depth);
    });

    toggleChildren(parent);

  } else if (parent.children && parent.children.length == 0) {
    parent.children = null;
  }

  return parent.max_depth;
}

var inf = 100; //ONLY USED FOR OPTIONAL ARGUMENT STUFF
function initQuantities(d) {
    if (isFunction(d))  initFunction(d);
    else                initPosition(d);
    d.init = true;
}

/**
Converts the 3-leveled JSON (function-qoa-position) to 2 levels (function-
position) and store the qoas in a side array.
*/
function initFunction(func_node) {
  if (!func_node._children || func_node._children.length == 0)
    return;
  else if (func_node._children.length == 1) {
    var single_qoa = func_node._children[0];
    func_node.quantities = [single_qoa];
    func_node._children = single_qoa;
    func_node.quantity_index = 0;
    return;
  }

  //Put all varieties of argument numbers into one array, and remember the max.
  var quantities = [];
  var max_qoa = 0;
  for (var qoa_index = 0; qoa_index < func_node._children.length; ++qoa_index) {
    var qoa_node = func_node._children[qoa_index];
    qoa_node.frequency = qoa_node.frequency;
    quantities.push(qoa_node);
    max_qoa = Math.max(max_qoa, qoa_node.qoa);
  }
  //End of loop: quantities array contains all "qoa" objects contained in
  //func_node, max_qoa contains the maximum qoa value.

  //Create an array that will represent all quantities combined into one tree,
  //which will be the default tree.
  var all = {
    qoa: inf,
    children: null,
    _children: [],
    frequency: 0,
    parent: func_node
  };
  for (var index = 0; index < max_qoa; ++index) {
    all._children.push({ position: (index + 1), _children: [], frequency: 0 });
  }
  //End of loop: all object's children array has length = max_qoa

  //Take all the children from these nodes and put them in the appropriate
  //index in my "all" array.
  for (var qoa_index = 0; qoa_index < func_node._children.length; ++qoa_index) {
    var qoa_node = func_node._children[qoa_index];
    all.frequency += qoa_node.frequency;

    for (arg_index = 0; arg_index < qoa_node._children.length; ++arg_index) {
      var arg_node = qoa_node._children[arg_index];
      var position = all._children[arg_node.position - 1];

      arg_node.parent = func_node;
      position.frequency += arg_node.frequency;
      position._children = position._children.concat(arg_node._children);
    }
  }

  quantities.push(all);
  func_node.quantities = quantities;
  setInfo(func_node, all);
  func_node.quantity_index = quantities.length - 1;

  func_node.quantities.forEach(console.log);
}

/**
The initFunction above modifies the contents of the position file to contain
possible duplicates for functions -- this will consolidate them.
*/
function initPosition(position_node) {
  consolidate(position_node, "function");
  truncateLongList(position_node);
}

function consolidate(node, duped_field) {
  if (!node._children) return;

  //consolidates
  var unique_children = {}

  for (var index = 0; index < node._children.length; ++index) {
    var node_child = node._children[index],
        id_field = node_child[duped_field],
        unique_child = unique_children[id_field];

    if (unique_child != null) {

      unique_child.frequency += node_child.frequency;
      if (isLeaf(node_child))
        unique_child.allExamples.forEach(node_child.allExamples.add);
      else
        unique_child._children = unique_child._children.concat(node_child.children);

    } else {

      if (isLeaf(node_child))
        node_child.allExamples = d3.set(node_child.allExamples);
      unique_children[id_field] = node_child;

    }
  }

  node._children = [];
  Object.keys(unique_children).forEach(function(key) {
    node._children.push(unique_children[key]);
  });
}

function truncateLongList(position_node) {
  //Sorts arguments by frequency, leaves only the first 10 and hides
  //the rest.
  if (!isFunction(position_node) && position_node._children.length > 10) {
      position_node._children.sort(function(a, b) {
          return b.frequency - a.frequency;
      });
      position_node._holding = position_node._children.splice(10);
      position_node._children.splice(position_node._children.length, 0, {
          "function": "",
          "frequency": -1,
          "parent": this
      });
  }
}

function setInfo(accept, give) {
  accept.children    = give.children;
  accept._children   = give._children;
  accept.example     = give.example;
  accept.frequency   = give.frequency;
}

//If the function does not have a position value set, then it is an argument node.
function isFunction(d) {
    return d.function != null;
}

function isPosition(d) {
  return d.position != null;
}

function isLeaf(d) {
  return d.allExamples != null;
}

//Works with the idea that the "all" variant is the final one in the
//`quantities` array.
function isParentShowingAll(d) {
  var par = d.parent;
  if (!par || !isFunction(par) || isLeaf(par))
    return false;
  else
    return par.quantity_index == par.quantities.length - 1;
}

/**
 * Central function; updates what the graph looks like when the nodes change.
 */
var node_num = 0;
function update(src) {
    var nodes = tree.nodes(root),
        links = tree.links(nodes);

    nodes.forEach(function(d) {
        if (d.depth % 2 == 0) {
            d.y = d.depth * interfunc_gap;
        } else {
            d.y = (d.depth - 1) * interfunc_gap + func_arg_gap;
        }
    })

    var node = svg.selectAll("g")
        .data(nodes, function(d) {
            return d.id || (d.id = ++node_num);
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

    //center(src);
}

/**
 * Creates both circular function nodes and rectangular function argument nodes.
 */
function enterNode(node, src) {
    var nodeEnter = node.enter()
        .append("g")
        .attr("class", "node")
        .attr("id", function(d) {
            return "n" + d.id;
        })
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
        rects = nodeEnter.filter(function(d) {
            return d.depth % 2 != 0;
        })

    circles.on("mouseover", mouseover)
        .on("mousemove", mousemove)
        .on("mouseleave", mouseleave)
        .on("dblclick", WindowMaker.makeWindow)
        .on("click", click);
    rects.on("click", click)
        .on("mouseover", rect_mouseover)
        .on("mouseleave", rect_mouseout);
    expand.on("click", expandclick);

    //ENTER CIRCLES//////////////////////////////////////////
    circles.append("circle")
        .attr("r", 1e-6);

    circles.append("text")
        .attr("dx", function(d) {
            return -scale(d.frequency) - 2;
        })
        .attr("dy", 3)
        .attr("text-anchor", "end")
        .text(function(d) {
            return d.function;
        });

    //ENTER RECTANGLES////////////////////////////////////////
    rects.append("rect")
        .attr("width", 1e-6)
        .attr("height", 1e-6)
        .attr("x", -square_side_half)
        .attr("y", -square_side_half);

    rects.append("text")
        .text(function(d) {
            return d.position;
        })
        .attr("dx", function(d) {
            return d.position > 9 ? -7 : -3;
        })
        .attr("dy", 5);

    //ENTER EXPAND NODES//////////////////////////////////////
    expand.append("polygon")
        .attr("height", "20")
        .attr("width", "20")
        .attr("points", "0,0 0,0 0,0")
        //.attr("points", "-10,-5 0,15 10,-5")
        .style("fill", expand_col)
        .on("mouseover", function(d) {
            d3.select("#n" + d.id).select("polygon")
                .style("fill", expand_hover);
        })
        .on("mouseleave", function(d) {
            d3.select("#n" + d.id).select("polygon")
                .style("fill", expand_col);
        })
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

var b = "<b>",
    bb = "</b>",
    i = "<i>",
    ii = "</i>",
    br = "<br/>"; //Helpful markup.
function mouseover(d) {
    var func = "func: " + b + d.function + bb + br,
        freq = "count: " + d.frequency.toLocaleString() + br;
        depth = "depth: " + (d.depth/2) + br; //+ " of max " + d.max_depth + br,
        id = d.example;

    var hovered = d3.select("#n" + d.id);

    var ex;
    /**if (d.fullExample) {
        ex = "ex: " + i + d.fullExample + ii + br;
    } else {
        ex = "ex: " + i + "unavailable" + ii + br;
        d3.json("examples.php?id=" + id, function(error, data) {
            if (error) throw error;
            else if (!data) return;

            d.fullExample = data["formula"].replace(/</g, "&lt;").replace(/>/g, "&gt;");
            var ex = "ex: " + i + d.fullExample + ii + br;
            tip.attr("height", null).html(func + freq + depth + ex)
        });
    }*/

    tip.html(func + freq + depth + ex)
        .style("left", (d3.event.pageX + tip_x) + "px")
        .style("top", (d3.event.pageY + tip_y) + "px")
        .style("display", "inline");

    hovered.select("circle")
        .style("fill", function(d) {
            if (d._children) return circle_hover; //Get darker color.
            else if (d.children) return empty_hover; //Get gray.
            else return empty_col; //Don't change if no children.
        });

    /*d3.selectAll("svg g").style("fill-opacity", ".25");
    (function opaqueTree(c) {
      d3.select("#n" + c.id).style("fill-opacity", 1);
      if (c.parent) opaqueTree(c.parent);
    })(d);*/
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
    if (!tip.hover)
        tip.style("display", "none");

    var hovered = d3.select("#n" + d.id);

    hovered.select("circle")
        .style("fill", function(d) {
            return d._children ? circle_col : (d.children ? circle_empty : empty_col);
        });

    //d3.selectAll("svg g").style("fill-opacity", "1");
}

function rect_mouseover(d) {
    d3.select("#n" + d.id).select("rect").style("fill", function(d) {
        if (d._children) return rect_hover;
        else return empty_hover;
    });
}

function rect_mouseout(d) {
    d3.select("#n" + d.id).select("rect").style("fill", function(d) {
        return d._children ? (isParentShowingAll(d)
          ? rect_col : alternate_rect) : rect_empty; //SKYBLUE
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
        .attr("r", function(d) {
            return scale(d.frequency);
        })
        .style("fill", function(d) {
            return d._children ? circle_col : (d.children ? circle_empty
              : empty_col);
        });

    nodeUpdate.select("rect:not(.tooltip)")
        .attr("width", square_side)
        .attr("height", square_side)
        .style("fill", function(d) {
            return d._children ? (isParentShowingAll(d) ? rect_col
              : alternate_rect) : rect_empty; //SKYBLUE
        });

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
        if (!d._children && !d.children && d.frequency > 0) {
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
            return "translate(" + y + "," + x + ")";
        })
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
            var o = {
                x: src.x0,
                y: src.y0
            };
            return diagonal({
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
        .attr("d", diagonal);
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
            return diagonal({
                source: o,
                target: o
            });
        })
        .remove();
}

/**
 * Expands or dismisses children nodes when a given node is clicked.
 */
function click(d) {
    //Circles just have toggle functionality: click, and see all.
    if (d3.event.shiftKey || d3.event.ctrlKey) {
        changeQuantities(d);
    } else if (d3.event.altKey) {
        expandSubLevel(d);
    } else {
        toggleChildren(d);
    }

    update(d);
}

/**
 * If children are currently shown, they are hidden. And vice versa.
 */
function toggleChildren(d) {
    if (d.children) {
        d._children = d.children;
        d.children = null;
    } else {
        if (!d.init) initQuantities(d);
        d.children = d._children;
        d._children = null;
    }
}

/**
 * Toggles all of the clicked node's children.
 * If some are expanded and some not, it expands all the remaining and stops.
 * If the click node's children aren't shown, it shows them and then toggles
 *   children too.
 */
function expandSubLevel(d) {
    if (!d.children && d._children)
        toggleChildren(d);

    var expandNotReduce = false;
    var children = d.children || d._children;
    if (!children) return; //Nothing to see here.
    if (d._holding) children.concat(d._holding);

    children.forEach(function(cd) {
        if (!cd.children && cd._children)
            expandNotReduce = true;
    });

    if (!expandNotReduce) {
        children.forEach(toggleChildren);
    } else {
        children.filter(function(cd) {
                return cd.children == null;
            })
            .forEach(toggleChildren);
    }
}

/**
 * Iterates through the various numbers of arguments possible for this function.
 * Begins at default (where are possibilities are shown at once).
 * Shift click sends it to the lowest possible number of arguments, then goes up.
 * Ctrl click goes to highest possible first, then goes down.
 * When they reach they end, they revert to the default.
 */
function changeQuantities(d) {
    if (!isFunction(d) || !d.children || d.quantities.length <= 1) return;

    if (d3.event.shiftKey) {
        d.quantity_index = (d.quantity_index + 1) % d.quantities.length;
    } else if (d3.event.ctrlKey) {
        d.quantity_index = (d.quantity_index == 0) ? d.quantities.length - 1
          : d.quantity_index - 1;
    }

    var replacement = d.quantities[d.quantity_index];

    setInfo(d, replacement);
    if (!d.children) toggleChildren(d);
    //Reset tooltip.
    mouseleave(d);
    mouseover(d);
}

/**
 * When you click on the arrow that hides the possible functions beyond the
 * tenth most commonly used, this shows the rest (or hides them if shown already)
 */
function expandclick(d) {
    var par = d.parent;

    if (par._holding) {
        par.children = par.children.concat(par._holding);
        par._holding = null;
    } else {
        par._holding = par.children.splice(10)
            .filter(function(i) {
                return i.frequency != -1;
            });
        par.children.splice(par.children.length, 0, d);
    }

    update(par);
}

//http://bl.ocks.org/robschmuecker/7880033
function center(d) {
    var scale = zoomer.scale(),
        x = -d.y0 * scale + (width / 2),
        y = -d.x0 * scale + (height / 2);
    d3.select("g").transition().duration(duration)
        .attr("transform", "translate(" + x + "," + y + ")scale(" + scale + ")");
    zoomer.scale(scale);
    zoomer.translate([x, y]);
}
