"""Draws the report charts from results/*.csv into results/plots/.

Usage:  python3 scripts/plot_results.py        (needs matplotlib only)
"""
import csv
import math
import os

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTS = os.path.join(ROOT, "results")
PLOTS = os.path.join(RESULTS, "plots")

# validated categorical slots (blue, orange, aqua) and neutral inks
BLUE, ORANGE, AQUA = "#2a78d6", "#eb6834", "#1baf7a"
INK, INK_2, INK_3, GRID, SURFACE = "#0b0b0b", "#52514e", "#8a8984", "#e4e3df", "#fcfcfb"

STRUCTURE_COLOR = {"DynamicArray": BLUE, "MyLinkedList": ORANGE, "MinHeap": AQUA}
METRIC_COLOR = {"steps": BLUE, "moves": ORANGE, "comparisons": AQUA}
STRUCTURE_STYLE = {"DynamicArray": "-", "MyLinkedList": "--", "MinHeap": "-"}

plt.rcParams.update({
    "figure.facecolor": SURFACE,
    "axes.facecolor": SURFACE,
    "axes.edgecolor": INK_3,
    "axes.labelcolor": INK_2,
    "axes.titlecolor": INK,
    "axes.titlesize": 11,
    "axes.labelsize": 10,
    "xtick.color": INK_2,
    "ytick.color": INK_2,
    "legend.frameon": False,
    "legend.fontsize": 8.5,
    "legend.handlelength": 3.2,
    "font.size": 10,
    "axes.spines.top": False,
    "axes.spines.right": False,
})


def read_csv(name):
    with open(os.path.join(RESULTS, name), newline="") as f:
        return list(csv.DictReader(f))


def series(rows, key, **filters):
    picked = [r for r in rows if all(r[k] == v for k, v in filters.items())]
    picked.sort(key=lambda r: int(r["n"]))
    return [int(r["n"]) for r in picked], [float(r[key]) for r in picked]


def style_axes(ax, ylabel, title):
    ax.set_xscale("log")
    ax.set_yscale("log")
    ax.set_xlabel("n (number of elements, log scale)")
    ax.set_ylabel(ylabel)
    ax.set_title(title, loc="left")
    ax.grid(True, which="major", color=GRID, linewidth=0.8)
    ax.set_axisbelow(True)


def end_label(ax, xs, ys, text):
    ax.annotate(text, (xs[-1], ys[-1]), xytext=(6, 0), textcoords="offset points",
                va="center", fontsize=8.5, color=INK_2)


def time_panel(ax, rows, workload, variant, structures):
    for s in structures:
        xs, ys = series(rows, "time_ms", workload=workload, variant=variant, structure=s)
        ax.plot(xs, ys, STRUCTURE_STYLE[s], color=STRUCTURE_COLOR[s], linewidth=2,
                marker="o", markersize=6, markeredgecolor=SURFACE, markeredgewidth=1.5, label=s)
        end_label(ax, xs, ys, f"{ys[-1]:.3g} ms")
    style_axes(ax, "time, ms (median of 5 runs, log scale)", "Time vs n")
    if len(structures) > 1:
        ax.legend(loc="upper left")
    ax.margins(x=0.15)


def ops_panel(ax, rows, workload, variant, structures):
    for s in structures:
        for metric, color in METRIC_COLOR.items():
            xs, ys = series(rows, metric, workload=workload, variant=variant, structure=s)
            if not any(ys):
                continue  # a counter that is always 0 cannot be drawn on a log axis
            ax.plot(xs, ys, STRUCTURE_STYLE[s], color=color, linewidth=2, marker="o", markersize=5,
                    markeredgecolor=SURFACE, markeredgewidth=1.2,
                    label=f"{metric} - {s}" if len(structures) > 1 else metric)
    style_axes(ax, "operation count (log scale)", "Steps / moves / comparisons vs n")
    ax.legend(loc="upper left")
    if len(structures) == 2:
        a = series(rows, "steps", workload=workload, variant=variant, structure=structures[0])[1]
        b = series(rows, "steps", workload=workload, variant=variant, structure=structures[1])[1]
        if a == b:
            ax.text(0.98, 0.04, "both structures perform exactly the same\nnumber of steps and comparisons",
                    transform=ax.transAxes, ha="right", va="bottom", fontsize=8.5, color=INK_2)


def workload_figure(rows, workload, variant, structures, title, filename, reference=None):
    fig, (left, right) = plt.subplots(1, 2, figsize=(11, 4.3))
    time_panel(left, rows, workload, variant, structures)
    ops_panel(right, rows, workload, variant, structures)
    if reference:
        label, fn = reference
        xs = sorted({int(r["n"]) for r in rows})
        right.plot(xs, [fn(x) for x in xs], ":", color=INK_3, linewidth=1.5, label=label)
        right.legend(loc="upper left")
    note = "Counters that stay 0 for every n are omitted (log axis). Solid = DynamicArray, dashed = MyLinkedList." \
        if len(structures) > 1 else "Counters are counted inside the operations."
    fig.suptitle(title, x=0.01, ha="left", fontsize=12.5, color=INK, fontweight="bold")
    fig.text(0.01, 0.005, note, fontsize=8, color=INK_3)
    fig.tight_layout(rect=(0, 0.03, 1, 0.95))
    fig.savefig(os.path.join(PLOTS, filename), dpi=150)
    plt.close(fig)
    print("wrote", filename)


def memory_figure():
    rows = read_csv("memory.csv")
    fig, ax = plt.subplots(figsize=(7.5, 4.5))
    for s, style in (("DynamicArray", "-"), ("MyLinkedList", "-"), ("MinHeap", "--")):
        xs, ys = series(rows, "mb", structure=s)
        ax.plot(xs, ys, style, color=STRUCTURE_COLOR[s], linewidth=2, marker="o", markersize=6,
                markeredgecolor=SURFACE, markeredgewidth=1.5, label=s)
        _, per = series(rows, "bytes_per_element", structure=s)
        if s != "MinHeap":
            end_label(ax, xs, ys, f"{ys[-1]:.1f} MB ({per[-1]:.1f} B/elem)")
    style_axes(ax, "retained memory, MB (JOL GraphLayout, log scale)", "Memory footprint vs n")
    ax.legend(loc="upper left")
    ax.set_xlim(60, 2e7)
    fig.text(0.01, 0.005, "MinHeap uses the same int[] layout as DynamicArray, so its line overlaps.",
             fontsize=8, color=INK_3)
    fig.tight_layout(rect=(0, 0.03, 1, 1))
    fig.savefig(os.path.join(PLOTS, "memory.png"), dpi=150)
    plt.close(fig)
    print("wrote memory.png")


def buildheap_figure():
    rows = read_csv("buildheap.csv")
    fig, (left, right) = plt.subplots(1, 2, figsize=(11, 4.3))
    variants = (("insert-random", BLUE, "-"), ("floyd-random", ORANGE, "-"),
                ("insert-descending", BLUE, "--"), ("floyd-descending", ORANGE, "--"))
    for ax, key, ylabel, title in ((left, "time_ms", "time, ms (median of 5 runs, log scale)", "Time vs n"),
                                   (right, "comparisons", "comparisons (log scale)", "Comparisons vs n")):
        for v, color, style in variants:
            xs, ys = series(rows, key, variant=v)
            ax.plot(xs, ys, style, color=color, linewidth=2, marker="o", markersize=5,
                    markeredgecolor=SURFACE, markeredgewidth=1.2, label=v)
        style_axes(ax, ylabel, title)
        ax.legend(loc="upper left")
    xs = sorted({int(r["n"]) for r in rows})
    right.plot(xs, [x * math.log2(x) for x in xs], ":", color=INK_3, linewidth=1.5, label="n·log2(n)")
    right.plot(xs, [2 * x for x in xs], "-.", color=INK_3, linewidth=1.2, label="2n")
    right.legend(loc="upper left")
    fig.suptitle("Bonus B - n x insert() vs Floyd buildHeap()", x=0.01, ha="left", fontsize=12.5,
                 color=INK, fontweight="bold")
    fig.text(0.01, 0.005, "Blue = n separate inserts, orange = Floyd. Solid = random input, "
             "dashed = descending input (worst case for insert).", fontsize=8, color=INK_3)
    fig.tight_layout(rect=(0, 0.03, 1, 0.95))
    fig.savefig(os.path.join(PLOTS, "buildheap.png"), dpi=150)
    plt.close(fig)
    print("wrote buildheap.png")


def main():
    os.makedirs(PLOTS, exist_ok=True)
    rows = read_csv("results.csv")
    lists = ("DynamicArray", "MyLinkedList")
    workload_figure(rows, "W1", "-", lists, "W1 - Random access: 10 000 x get(index)", "W1_random_access.png")
    workload_figure(rows, "W2", "-", lists, "W2 - Search: 1 000 x contains(x), half present",
                    "W2_search.png")
    workload_figure(rows, "W3", "head", lists, "W3 head - 1 000 inserts + 1 000 removes at index 0",
                    "W3_head.png")
    workload_figure(rows, "W3", "middle", lists, "W3 middle - 1 000 inserts + 1 000 removes at index n/2",
                    "W3_middle.png")
    workload_figure(rows, "W4", "-", ("MinHeap",), "W4 - Priority processing: n x insert + n x extractMin",
                    "W4_priority.png", reference=("n·log2(n)", lambda x: x * math.log2(x)))
    if os.path.exists(os.path.join(RESULTS, "buildheap.csv")):
        buildheap_figure()
    if os.path.exists(os.path.join(RESULTS, "memory.csv")):
        memory_figure()


if __name__ == "__main__":
    main()
