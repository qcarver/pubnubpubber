/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var diameter = 600,
        width = diameter,
        height = diameter,
        padding = 1.5, // separation between same-color nodes
        clusterPadding = 6, // separation between different-color nodes
        maxRadius = 120,
        n = 200, // total number of nodes
        m = 10; // number of distinct clusters;

function drawBubbles(nodes) {

// Use the pack layout to initialize node positions.
    var bubbles = d3.layout.pack()
            .sort(null)
            .size([width, height])
            .children(function (d) {
                return d.values;
            })
            .value(function (d) {
                return d.size;
            })
            .nodes({values: d3.nest()
                        .key(function (d) {
                            return d.className;
                        })
                        .entries(nodes)});

    var force = d3.layout.force()
            .nodes(nodes)
            .size([width, height])
            .gravity(.02)
            .charge(0)
            .on("tick", tick)
            .start();

    var svg = d3.select("body").append("svg")
            .attr("width", width)
            .attr("height", height);

    var node = svg.selectAll("g")
            .data(nodes)
            .enter()
            .append("g")
            .call(force.drag);

    var circle = node.append("circle")
            .attr('class', function (d) {  //QC d corresponds to the data from your dataset (for that node)
                return d.className;
            })
            .attr("id", function (d) {
                return "circle-" + d.uid;
            });

    //make a clipping path 
    node.append("clipPath")
            .attr("id", function (d) {
                return "clip-" + d.uid + "";
            })
            //use the circle we just made to define the path
            .append("use")
            .attr("xlink:href", function (d) {
                return "#circle-" + d.uid;
            });

    node.append("text")
            .attr("dx", function (d) {
                return -20;
            })
            .attr("clip-path", function (d) {
                return "url(#clip-" + d.uid + ")";
            })
            .text(function (d) {
                return d.className;
            });

    node.append('emoji')
            .attr('symbol', function (d) {
                return emojiHash(d.name);
            })
            .attr('width', function (d) {
                return d.size;
            })
            .attr('height', function (d) {
                return d.size;
            })
            .attr('opacity', 255)
            .attr("transform", function (d) {
                return "translate(" + (-.5 * d.size) + "," + (-.5 * d.size) + ")";
            });

    circle.transition()
            .duration(750)
            .delay(function (d, i) {
                return i * 5;
            })
            .attrTween("r", function (d) {
                var i = d3.interpolate(0, d.size);
                return function (t) {
                    return d.size = i(t);
                };
            });


    function tick(e) {
        node
                .each(cluster(10 * e.alpha * e.alpha))  //QC cluster is a method (below)
                .each(collide(.5))
                //for each object.. circles adjust cx, cy,.. text adjust ?tx?ty? et...
                .attr("transform", function (d) {
                    return "translate(" + d.x + "," + d.y + ")";
                });
    }

// Move d to be adjacent to the cluster node.
    function cluster(alpha) {
        return function (d) {
            var cluster = kingPin[d.className];
            if (cluster === d)
                return;
            var x = d.x - cluster.x,
                    y = d.y - cluster.y,
                    l = Math.sqrt(x * x + y * y),
                    r = d.size + cluster.size;
            if (l != r) {
                l = (l - r) / l * alpha;
                d.x -= x *= l;
                d.y -= y *= l;
                cluster.x += x;
                cluster.y += y;
            }
        };
    }

// Resolves collisions between d and all other circles.
    function collide(alpha) {
        var quadtree = d3.geom.quadtree(nodes);
        return function (d) {
            var r = d.size + maxRadius + Math.max(padding, clusterPadding),
                    nx1 = d.x - r,
                    nx2 = d.x + r,
                    ny1 = d.y - r,
                    ny2 = d.y + r;
            quadtree.visit(function (quad, x1, y1, x2, y2) {
                if (quad.point && (quad.point !== d)) {
                    var x = d.x - quad.point.x,
                            y = d.y - quad.point.y,
                            l = Math.sqrt(x * x + y * y),
                            r = d.size + quad.point.size + (d.className === quad.point.className ? padding : clusterPadding);
                    if (l < r) {
                        l = (l - r) / l * alpha;
                        d.x -= x *= l;
                        d.y -= y *= l;
                        quad.point.x += x;
                        quad.point.y += y;
                    }
                }
                return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
            });
        };
    }

    //hash any given string to an emoji name
    function emojiHash(hashMe) {
        return Object.keys(emojiMap)[Math.abs(hashMe.hashCode()) % Object.keys(emojiMap).length];
    }
}
