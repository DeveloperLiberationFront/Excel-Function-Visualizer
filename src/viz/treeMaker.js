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
var zoomer = d3.behavior.zoom()
    .scaleExtent([.1, 8])
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
var src = "json/j" + getQueryVariable("file") + ".json"; //If python server started in tree folder
var root;
d3.json(src, function(error, json) {
    if (error) throw error;

    root = json;
    root.x0 = 0;
    root.y0 = 0;

    scale = d3.scale.log()
        .domain([1, root.frequency])
        .range([5, 25])
        .nice();

    collapseAll(root);
    flattenJSON(root);
    toggleChildren(root);

    center(root);
    update(root);
})

function collapseAll(parent) {
  parent.longest_path = 0;

  if (parent.children) {
    var len = parent.children.length;

    if (len > 0) {
      var child_is_new_level = isFunction(parent) ? 0 : 1
      parent.children.forEach(function(child) {
        var longest_of_children = collapseAll(child),
            possible_max = longest_of_children + child_is_new_level;
        parent.longest_path = Math.max(possible_max, parent.longest_path);
      });

      toggleChildren(parent);
    } else if (len == 0) {
      parent.children = null;
      parent._children = null;
    }
  }

  return parent.longest_path;
}

var inf = 100; //ONLY USED FOR OPTIONAL ARGUMENT STUFF
function flattenJSON(node) {
    if (isFunction(node)) initFunction(node);
    else                  initPosition(node);
    node.init = true;
}

/**
Converts the 3-leveled JSON (function-qoa-position) to 2 levels (function-
position) and store the qoas in a side array.
*/
function initFunction(func_node) {
  //PRECONDITION: collapseAll has been called, and nodes, if
  //              they have any children, have them in _children
  var kids = func_node._children;

  //Setting children to null ensures that no node has
  //a children array of length 0.
  if (!kids) return;

  consolidate(func_node, "qoa");
  kids.forEach(function(child) { consolidate(child, "position"); });

  //Put all varieties of argument numbers into one array, and remember the max.
  var quantities = [],
      max_qoa = 0;
  for (var qoa_index = 0; qoa_index < kids.length; ++qoa_index) {
    var qoa_node = kids[qoa_index];
    quantities.push(qoa_node);
    max_qoa = Math.max(max_qoa, qoa_node.qoa);
  }

  var default_info;
  if (quantities.length > 1) {
    default_info = combineAllQuantities(quantities, max_qoa);
    quantities.push(default_info);
  } else if (quantities.length == 1) {
    default_info = kids[0];
  }

  default_info["example"] = func_node["example"];
  func_node.quantities = quantities;
  setInfo(func_node, default_info);
  func_node.quantity_index = quantities.length - 1;
}

function combineAllQuantities(quantities, max_qoa) {
  //Create an array that will represent all quantities combined into one tree,
  //which will be the default tree.
  var positions = [];
  for (var index = 0; index < max_qoa; ++index) {
    positions.push({ position: (index + 1), _children: [], frequency: 0 });
  }

  var all_qoa = {
    qoa: inf,
    children: null,
    _children: positions,
    frequency: 0,
  };

  //Take all the children from these nodes and put them in the appropriate
  //index in my "all" array.
  for (var qoa_index = 0; qoa_index < quantities.length; ++qoa_index) {
    var qoa_node = quantities[qoa_index];
    all_qoa.frequency += qoa_node.frequency;

    for (var arg_index = 0; arg_index < qoa_node._children.length; ++arg_index) {
      var arg_node = qoa_node._children[arg_index],
          position = all_qoa._children[arg_node.position - 1];

      position.frequency += arg_node.frequency;
      position._children = position._children.concat(arg_node._children);
    }
  }

  return all_qoa;
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

  var unique_children = {}
  for (var index = 0; index < node._children.length; ++index) {
    var node_child = node._children[index],
        id_field = node_child[duped_field],
        unique_child = unique_children[id_field];

    if (unique_child == null) {

      var clone = JSON.parse(JSON.stringify(node_child));
      if (isLeaf(clone))
        clone.allExamples = d3.set(node_child.allExamples);
      unique_children[id_field] = clone;

    } else {

      unique_child.frequency += node_child.frequency;
      if (isLeaf(node_child)) {
        node_child.allExamples.forEach(function(ex) {
          unique_child.allExamples.add(ex);
        });
      } else
        unique_child._children = unique_child._children.concat(node_child._children);

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
  var kids = position_node._children;
  if (isPosition(position_node) && kids && kids.length > 10) {
      kids.sort(function(a, b) {
          return b.frequency - a.frequency;
      });

      position_node._holding = kids.splice(10);

      kids.splice(kids.length, 0, {
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

function isArrow(d) {
  return d.frequency == -1;
}

//Works with the idea that the "all" variant is the final one in the
//`quantities` array.
function isParentShowingAll(d) {
  var par = d.parent;
  return isShowingAll(par);
}

function isShowingAll(node) {
  if (!node || !isFunction(node) || isLeaf(node))
    return false;
  else
    return node.quantity_index == node.quantities.length - 1;
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

    rects.on("mouseover", mouseover)
        .on("mousemove", mousemove)
        .on("mouseleave", mouseleave);

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
        freq = "count: " + d.frequency.toLocaleString() + br,
        depth = "depth: " + (d.depth/2) + br, //+ " of max " + d.longest_path + br
        ex = d.example;

    var hovered = d3.select("#n" + d.id),
        pageX = (d3.event && d3.event.pageX) || 0,
        pageY = (d3.event && d3.event.pageY) || 0;

    tip.html((d.function ? func + freq + depth + ex : freq))
        .style("left", (pageX + tip_x) + "px")
        .style("top", (pageY + tip_y) + "px")
        .style("display", "inline");

    hovered.select("circle")
        .style("fill", function(d) {
            if (d._children) return circle_hover; //Get darker color.
            else if (d.children) return empty_hover; //Get gray.
            else return empty_col; //Don't change if no children.
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
    //if (d3.event.shiftKey && d3.event.ctrlKey) {
    //    test(d);
    //} else 
    if (d3.event.shiftKey || d3.event.ctrlKey) {
        changeQuantities(d, d3.event.shiftKey);
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
        if (!d.init) flattenJSON(d);
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
function changeQuantities(d, goUpIfTrue) {
    if (!isFunction(d) || !d.children || d.quantities.length <= 1) return;

    if (goUpIfTrue) {
        d.quantity_index = (d.quantity_index + 1) % d.quantities.length;
    } else {
        d.quantity_index = (d.quantity_index == 0) ? d.quantities.length - 1
          : d.quantity_index - 1;
    }

    var replacement = d.quantities[d.quantity_index];

    setInfo(d, replacement);
    if (!d.children) toggleChildren(d);

    mouseleave(d);
    mouseover(d);
}

/**
 * When you click on the arrow that hides the possible functions beyond the
 * tenth most commonly used, this shows the rest (or hides them if shown already)
 */
function expandclick(d) {
    if (!isArrow(d)) return; //ONLY WORKS WITH THE ARROWS

    var par = d.parent;
    toggleHoldingNodes(par);
    update(par);
}

function toggleHoldingNodes(par) {
  if (par._holding) {
      par.children = par.children.concat(par._holding);
      par._holding = null;
  } else if (par.children.length > 10) {
      var arrow = par.children.pop();
      par._holding = par.children.splice(10);
      par.children.splice(par.children.length, 0, arrow);
  }
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

var nodes_tested = 0;
function test(parent) {
  console.log("Testing node " + (++nodes_tested).toString() + "...");
  if (isLeaf(parent)) return;

  flattenJSON(parent);
  if (isFunction(parent) && !isArrow(parent)) {

    for (var qoa_index = 0; qoa_index < parent.quantities.length; ++qoa_index) {
      if (!parent.children) toggleChildren(parent);
      if (!parent.children) continue; //if still no children, there's nothing.
      changeQuantities(parent, true);

      testAllChildren(parent);
      freqCheckFunction(parent);
    }

  } else if (isPosition(parent)) {

    if (!parent.children) toggleChildren(parent);
    if (parent._holding) toggleHoldingNodes(parent);

    testAllChildren(parent);
    freqCheckPosition(parent);

  }
}

function testAllChildren(parent) {
  for (var child_index = 0; child_index < parent.children.length; ++child_index) {
    var child = parent.children[child_index];
    test(child);
  }
}

function freqCheckPosition(parent) {
  var freqOfChildren = 0;
  parent.children.forEach(function(child) {
    freqOfChildren += child.frequency;
  });

  if (parent.children.length > 10) ++freqOfChildren; //to compensate for arrow -1

  if (parent.frequency != freqOfChildren) {
    update(parent);
    throw "Frequencies don't match: " + parent.frequency.toString()
      + " != " + freqOfChildren.toString();
  }
}

function freqCheckFunction(parent) {
    var showingAll = isShowingAll(parent);

    parent.children.forEach(function(child) {
      if ((showingAll && parent.frequency < child.frequency) ||
          (!showingAll && parent.frequency != child.frequency)) {
        update(parent);
        throw "Frequencies don't match: " + parent.frequency.toString()
          + " ~ " + child.frequency.toString();
      }
    });
}
