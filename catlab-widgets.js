import * as r from "d3";
function j(f) {
  const { width: a, height: c, layout: u } = f;
  let s;
  return {
    mount: (o) => {
      s = r.select(o).append("svg").attr("width", a).attr("height", c).attr("viewBox", [-a / 2, -c / 2, a, c]);
      s.append("g");
    },
    update: (o) => {
      const i = r.forceSimulation(o.nodes).force("link", r.forceLink(o.links).id((d) => d._id)).force("charge", r.forceManyBody().strength(-u.strength)).force("x", r.forceX()).force("y", r.forceY()).force("center", r.forceCenter(0, 0)), g = (t) => {
        function p(n) {
          n.active || t.alphaTarget(0.3).restart();
          const e = n.subject;
          e.fx = e.x, e.fy = e.y;
        }
        function x(n) {
          const e = n.subject;
          e.fx = n.x, e.fy = n.y;
        }
        function y(n) {
          n.active || t.alphaTarget(0);
          const e = n.subject;
          e.fx = null, e.fy = null;
        }
        return r.drag().on("start", p).on("drag", x).on("end", y);
      }, l = s.select("g").selectAll(".link").data(o.links, (d) => d._id).join("g").attr("class", "link");
      l.append("line").attr("stroke", "#000");
      const d = s.select("g").selectAll(".node").data(o.nodes, (d) => d._id).join("g").attr("class", "node").call(g(i));
      d.append("circle").attr("r", 5).attr("stroke", "#000").attr("fill", "#000"), d.append("text").attr("y", -10).attr("text-anchor", "middle").style("font-size", "8pt").text((t) => t.label), i.on("tick", () => {
        l.select("line").attr("x1", (t) => t.source.x).attr("y1", (t) => t.source.y).attr("x2", (t) => t.target.x).attr("y2", (t) => t.target.y), d.attr("transform", (t) => `translate(${t.x}, ${t.y})`);
      });
    },
    unmount: () => {
    }
  };
}
export {
  j as forceDirectedGraph
};
