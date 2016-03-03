/**
 * Makes the tree.
 * https://bl.ocks.org/mbostock/4339184
 */
 var margin = {
       top: 50,
       bottom: 50,
       left: 100,
       right: 100
     },
     height = 2000 - margin.left - margin.right,
     width = 2000 - margin.top - margin.bottom;

 var tree = d3.layout.tree()
                     .size([height, width]);

 var svg = d3.select("body")
             .append("svg")
               .attr("width", width + margin.left + margin.right)
               .attr("height", height + margin.top + margin.bottom)
             .append("g")
               .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

 var lines = d3.svg.line()
                   .x(function(d) { return d.lx; })
                   .y(function(d) { return d.ly; })
                   .interpolate("linear");
                      /*.diagonal()
                        .projection(function(d) {
                        return [d.x, d.y];
                      })*/

 var src = ".\\sumtree.json"; //If python server started in tree folder

 function getLine(d) {
   var coords = [
     {lx: d.source.x, ly: d.source.y},
     {lx: d.target.x, ly: d.target.y}
   ];

   return lines(coords);
 }

 d3.json(src, function(error, json) {
   if (error) throw error;

   var nodes = tree.nodes(json),
       links = tree.links(nodes);

   var node = svg.selectAll("g.node")
                 .data(nodes)
                 .enter()
                 .append("g")
                  .attr("class", "node")
                  .attr("transform", function(d) {
                    return "translate(" + d.x + "," + d.y + ")";
                  });

   node.append("circle")
       .attr("r", 5)
       .attr("fill", "red");

   node.append("text")
       .attr("dx", function(d) {
         return d.arguments || d.possibleArguments ? -16 : 16;
       }).attr("dy", 3)
       .attr("text-anchor", function(d) {
         return d.arguments || d.possibleArguments ? "end" : "start";
       }).text(function(d) {
         return d.function || d.position;
       })

   var link = svg.selectAll("path")
                 .data(links)
                 .enter()
                 .append("path")
                  .attr("class", "link")
                  .attr("d", getLine);
 })

 d3.select(self.frameElement)
   .style("height", height + "px");
