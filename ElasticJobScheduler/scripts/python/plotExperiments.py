#  Copyright (c) 2023 Wagomu project.
#
#  This program and the accompanying materials are made available to you under
#  the terms of the Eclipse Public License 2.0 which accompanies this distribution,
#  and is available at https://www.eclipse.org/legal/epl-v20.html
#
#  SPDX-License-Identifier: EPL-2.0

import os
import sys
import re
from decimal import Decimal

import numpy as np
import pandas as pd
import plotly.graph_objects as go
import plotly.io as pio
from matplotlib import pyplot as plt
import importlib

script_dir = os.path.dirname(os.path.abspath(__file__))
if script_dir not in sys.path:
    sys.path.insert(0, script_dir)

# just small cosmetics, but could cause troubles
# pio.kaleido.scope.mathjax = None
OUTPUT_FILE_TYPE = "pdf"
WIDTH_PIXEL = 1200
HEIGHT_PIXEL = 640
FONTSIZE = 26

ANALYSES_PATH = ""
INPUT_PATH = ""
GLOBAL_MAP = {}
ARRAY_MALLEABLE_TO_RIGID = []
MALLEABLE_STRATEGIES = ["EvolvingEasyBackfilling"]
#MALLEABLE_STRATEGIES = ["MalleableStrategy"]
GENERATE_CSV_PER_EXP = True
GENERATE_IMAGE_PER_EXP = True
DRAW_WEB = False
FORMAT = "pdf"
ENABLE_ENGLISH_OUTPUT = True
SHRINK_GROW_DICT = {}
SHRINK_GROW_FILE = "shrink_grow.txt"
SHRINK_GROW_FILE_PATH = ""
ENABLE_ORDERING = True
# https://plotly.com/python/discrete-color/
color_mapping = {
    'Backfilling': '#636EFA',
    'EasyBackfilling': '#EF553B',
    'EvolvingEasyBackfilling': '#00CC96',
    'MalleableStrategy': '#9600cc',
    'FCFS': '#AB63FA',
    'Shrink Events': '#FFA15A',
    'Grow Events': '#19D3F3',
    'inside_color_a' : '#d6d6d6',
    'inside_color_b' : '#8c8c8c',
    'preShrinkTimeGLB': '#FF6692',
    'growTimeAPGASClean': '#B6E880',
    'postGrowTimeGLB': '#FF97FF',
    'shrinkTimeAPGASClean': '#FECB52',
    'answer_time': '#636EFA',
}

# Explicitly declare variables filled with dummy values
scheduler_algo = ['']
# List of expected configuration variables
config_variables = ['scheduler_algo']
scheduler_algo_reverse = ['']

DRAW_RANGE = 0

folder_map = {}
np.random.seed(42 + 1)
color_map = {key: (np.random.random(), np.random.random(), np.random.random()) for key in range(1, 100 + 2)}
MALLEABLE_TO_RIGID_RATE = set()

def main():
    if not os.path.exists(ANALYSES_PATH):
        os.mkdir(ANALYSES_PATH)

    for folder in os.listdir(INPUT_PATH):
        if ".idea" in folder or "plotExperiments.py" in folder:
            continue
        file_name = INPUT_PATH + "/" + folder + "/" + folder + "_output.txt"
        if os.path.exists(file_name):
            read_file(file_name, folder)

    ARRAY_MALLEABLE_TO_RIGID.extend([elem for elem in MALLEABLE_TO_RIGID_RATE])
    ARRAY_MALLEABLE_TO_RIGID.sort()

    if GENERATE_IMAGE_PER_EXP:
        draw_all_graphs()
    if GENERATE_CSV_PER_EXP:
        generate_csv_per_experiment()

    total_running_time()

    node_utilization()

    event_malleable()

    average_job_turnaround_time()

    computation_time = average_job_computation_time()

    waiting_time = average_job_waiting_time()

    average_job_turnaround_time_stacked(computation_time, waiting_time)

    answer_time = apgas_answer_time()

    start_time = apgas_start_time()

    shrink_grow_time(answer_time, start_time)


# https://plotly.com/python/bar-charts/
def average_job_turnaround_time_stacked(computation_time, waiting_time):
    draw_stacked_graph_with_range(waiting_time, computation_time, 0, '', 'Time in Seconds',
                                  "AverageJobTurnaroundTimeStacked")

def apgas_start_time():
    startup_times = []
    init_times = []
    startup_pattern = r"\[APGAS\] Place startup time: (\d+\.\d+) sec"
    init_pattern = r"Initialization time \(s\); (\d+\.\d+)"

    # Walk through all files in the directory
    for root, dirs, files in os.walk(INPUT_PATH):
        for file in files:
            file_path = os.path.join(root, file)
            with open(file_path, 'r', encoding="utf-8", errors="ignore") as f:  # Added errors="ignore" in case of encoding issues
                content = f.read()

                startup_matches = re.findall(startup_pattern, content)
                startup_times.extend([float(match) for match in startup_matches])

                init_matches = re.findall(init_pattern, content)
                init_times.extend([float(match) for match in init_matches])


    average_startup_time = sum(startup_times) / len(startup_times)
    print(f"Found {len(startup_times)} startup times.")
    print(f"Average startup time: {average_startup_time:.6f} sec")

    average_init_time = sum(init_times) / len(init_times)
    print(f"\nFound {len(init_times)} initialization times.")
    print(f"Average initialization time: {average_init_time:.6f} sec")

    with open(SHRINK_GROW_FILE_PATH, "a") as file:
        print('-' * 20, file=file)
        print('-' * 20, file=file)
        print("APGAS startup time:", average_startup_time, file=file)
        print('-' * 20, file=file)
        print('-' * 20, file=file)
        print("GLB initialization time:", average_init_time, file=file)

    return average_startup_time


def shrink_grow_time(answer_time, start_time):
    averages_dict = {}
    with open(SHRINK_GROW_FILE_PATH, "a") as file:
        print('-' * 20, file=file)
        print('-' * 20, file=file)
        nano = '1000000000'
        for event, (nbPlaces_list, time_list) in SHRINK_GROW_DICT.items():
            # Convert the lists to integers and calculate averages
            nbPlaces_average = sum(map(Decimal, nbPlaces_list)) / len(nbPlaces_list)
            time_average = sum(map(Decimal, time_list)) / len(time_list)
            if len(nbPlaces_list) != len(time_list):
                print('error in shrink_grow_time')

            averages_dict[event] = {'nbPlaces_average': nbPlaces_average, 'time_average': time_average,
                                    'number': len(time_list)}

        for event, averages in averages_dict.items():
            print(event, file=file)
            print(f'Number of Events: {averages["number"]}', file=file)
            print(f'Average Places: {averages["nbPlaces_average"]}', file=file)
            time_average_seconds = Decimal(averages["time_average"]) / Decimal(nano)
            print(f'Average Time in Seconds: {time_average_seconds}', file=file)
            timePlaceAvg = Decimal(averages["time_average"]) / Decimal(averages["nbPlaces_average"]) / Decimal(nano)
            print(f'Average Time per Place in Seconds: {timePlaceAvg}', file=file)
            print('-' * 20, file=file)

    #figure
    preShrinkTimeGLB = float(averages_dict['preShrinkTimeGLB']['time_average'] / Decimal(nano))
    postShrinkTimeGLB = float(averages_dict['postShrinkTimeGLB']['time_average'] / Decimal(nano))
    shrinkTimeAPGAS = float(averages_dict['shrinkTimeAPGAS']['time_average'] / Decimal(nano))
    preGrowTimeGLB = float(averages_dict['preGrowTimeGLB']['time_average'] / Decimal(nano))
    postGrowTimeGLB = float(averages_dict['postGrowTimeGLB']['time_average'] / Decimal(nano))
    growTimeAPGAS = float(averages_dict['growTimeAPGAS']['time_average'] / Decimal(nano))
    shrinkTimeAPGASClean = shrinkTimeAPGAS - preShrinkTimeGLB - postShrinkTimeGLB
    growTimeAPGASClean = growTimeAPGAS - preGrowTimeGLB - postGrowTimeGLB


    fig = go.Figure(data=[
        go.Bar(name='preShrink() of GLB', x=['Shrinking'], y=[preShrinkTimeGLB], marker=dict(color=color_mapping["preShrinkTimeGLB"])),
        go.Bar(name='postShrink() of GLB', x=['Shrinking'], y=[postShrinkTimeGLB]),
        go.Bar(name='shrinking of APGAS', x=['Shrinking'], y=[growTimeAPGASClean], marker=dict(color=color_mapping["growTimeAPGASClean"])),
        go.Bar(name='preGrow() of GLB', x=['Growing'], y=[preGrowTimeGLB]),
        go.Bar(name='postGrow() of GLB', x=['Growing'], y=[postGrowTimeGLB], marker=dict(color=color_mapping["postGrowTimeGLB"])),
        go.Bar(name='growing of APGAS', x=['Growing'], y=[shrinkTimeAPGASClean], marker=dict(color=color_mapping["shrinkTimeAPGASClean"])),
        go.Bar(name='APGAS to be malleable', x=['APGAS to be malleable'], y=[answer_time], marker=dict(color=color_mapping["answer_time"])),
    ])

    standoff = 10
    fig.update_layout(
        barmode='stack',
        yaxis=dict(
            title="Time in Seconds",
            titlefont_size=FONTSIZE,
            tickfont_size=FONTSIZE,
        ),
        xaxis=dict(
            titlefont_size=FONTSIZE,
            tickfont_size=FONTSIZE,
        ),
        width=WIDTH_PIXEL,
        height=HEIGHT_PIXEL,
        margin=dict(l=10, r=10, t=10, b=100),
        legend=dict(
            font=dict(size=FONTSIZE),
            traceorder="normal",
        ),
        yaxis_title_standoff=standoff,
    )

    if DRAW_WEB:
        fig.show()

    output = f"{ANALYSES_PATH}ShrinkGrowTimes.{FORMAT}"
    fig.write_image(output, format=OUTPUT_FILE_TYPE, engine="kaleido")

    #writing new csv/dat file for gnuplot
    output = f"{ANALYSES_PATH}ShrinkGrowTimes.dat"
    startup_apgas = start_time
    startup_glb = answer_time-start_time
    grow_apgas = growTimeAPGASClean
    grow_glb = preGrowTimeGLB + postGrowTimeGLB
    shrink_apgas = shrinkTimeAPGASClean
    shrink_glb = preShrinkTimeGLB + postShrinkTimeGLB
    content = f'''"operation", "APGAS", "GLB"
"Startup", {startup_apgas:.2f} ,{startup_glb:.2f}
"Grow", {grow_apgas:.2f}, {grow_glb:.2f}
"Shrink", {shrink_apgas:.2f}, {shrink_glb:.2f}'''
    with open(output, 'w') as f:
        f.write(content)


def apgas_answer_time():
    answer_time_sum = 0
    count = 0

    for item in GLOBAL_MAP:
        for job_id in GLOBAL_MAP[item]["jobs"]:
            job = GLOBAL_MAP[item]["jobs"][job_id]
            if job["JOB_CLASS"] == "evolving" and job["answer"] != 0:
                answer_time_sum += (job["answer"] - job["start"]) / 1000
                count += 1
            if job["JOB_CLASS"] == "malleable" and job["answer"] != 0:
                answer_time_sum += (job["answer"] - job["start"]) / 1000
                count += 1
    with open(SHRINK_GROW_FILE_PATH, "a") as file:
        print('-' * 20, file=file)
        print('-' * 20, file=file)
        print("APGAS answer time:", answer_time_sum / count, file=file)
    return answer_time_sum / count


def average_job_waiting_time():
    dic = {}
    for item in GLOBAL_MAP:
        strategy = GLOBAL_MAP[item]["strategy"]
        malleable = GLOBAL_MAP[item]["malleable_count"]

        if strategy not in dic:
            dic[strategy] = {}
        if malleable not in dic[strategy]:
            dic[strategy][malleable] = []

        sum_duration = 0
        count = 0
        for job_id in GLOBAL_MAP[item]["jobs"]:
            job = GLOBAL_MAP[item]["jobs"][job_id]
            duration = (job["start"] - job["insert"]) / 1000
            sum_duration += duration
            count += 1
        dic[strategy][malleable].append(sum_duration / count)

    res = {}

    for strategy in dic:
        tmp_dic = dic[strategy]

        if strategy not in res:
            res[strategy] = []

        for i in ARRAY_MALLEABLE_TO_RIGID:
            res[strategy].append(sum(tmp_dic[i]) / len(tmp_dic[i]))

    if ENABLE_ORDERING:
        res = {key: res[key] for key in scheduler_algo}

    if ENABLE_ENGLISH_OUTPUT:
        draw_graph_with_range(res, 0, '', 'Time in Seconds', "AverageJobWaitTime")
    else:
        draw_graph_with_range(res, 0, '', 'Zeit (s)', "Wartezeit")

    return res


def average_job_turnaround_time():
    dic = {}
    for item in GLOBAL_MAP:
        strategy = GLOBAL_MAP[item]["strategy"]
        malleable = GLOBAL_MAP[item]["malleable_count"]

        if strategy not in dic:
            dic[strategy] = {}
        if malleable not in dic[strategy]:
            dic[strategy][malleable] = []

        sum_duration = 0
        count = 0
        for job_id in GLOBAL_MAP[item]["jobs"]:
            job = GLOBAL_MAP[item]["jobs"][job_id]
            duration = (job["end"] - job["insert"]) / 1000
            sum_duration += duration
            count += 1
        dic[strategy][malleable].append(sum_duration / count)

    res = {}

    for strategy in dic:
        tmp_dic = dic[strategy]

        if strategy not in res:
            res[strategy] = []

        for i in ARRAY_MALLEABLE_TO_RIGID:
            res[strategy].append(sum(tmp_dic[i]) / len(tmp_dic[i]))

    if ENABLE_ORDERING:
        res = {key: res[key] for key in scheduler_algo}

    if ENABLE_ENGLISH_OUTPUT:
        draw_graph_with_range(res, 0, '', 'Time in Seconds', "AverageJobTurnaroundTime")
    else:
        draw_graph_with_range(res, 0, '', 'Zeit (s)', "Durchlaufzeit")

    return res


def average_job_computation_time():
    dic = {}
    for item in GLOBAL_MAP:
        strategy = GLOBAL_MAP[item]["strategy"]
        malleable = GLOBAL_MAP[item]["malleable_count"]

        if strategy not in dic:
            dic[strategy] = {}
        if malleable not in dic[strategy]:
            dic[strategy][malleable] = []

        sum_duration = 0
        count = 0
        for job_id in GLOBAL_MAP[item]["jobs"]:
            job = GLOBAL_MAP[item]["jobs"][job_id]
            duration = (job["end"] - job["start"]) / 1000
            sum_duration += duration
            count += 1
        dic[strategy][malleable].append(sum_duration / count)

    res = {}

    for strategy in dic:
        tmp_dic = dic[strategy]

        if strategy not in res:
            res[strategy] = []

        for i in ARRAY_MALLEABLE_TO_RIGID:
            res[strategy].append(sum(tmp_dic[i]) / len(tmp_dic[i]))

    if ENABLE_ORDERING:
        res = {key: res[key] for key in scheduler_algo}

    if ENABLE_ENGLISH_OUTPUT:
        draw_graph_with_range(res, 0, '', 'Time in Seconds', "AverageJobComputationTime")
    else:
        draw_graph_with_range(res, 0, '', 'Zeit (s)', "Rechenzeit")

    return res


def event_malleable():
    dic_shrink = {}
    dic_expand = {}

    for item in GLOBAL_MAP:
        strategy = GLOBAL_MAP[item]["strategy"]
        malleable = GLOBAL_MAP[item]["malleable_count"]
        data = GLOBAL_MAP[item]

        if malleable not in dic_shrink:
            dic_shrink[malleable] = []
        if malleable not in dic_expand:
            dic_expand[malleable] = []

        if strategy in MALLEABLE_STRATEGIES:
            shrink, expand = 0, 0
            for job_id in data["jobs"]:
                job = data["jobs"][job_id]
                for timestamp in job["tracking"]:
                    status = job["tracking"][timestamp]
                    if status["type"] in "shrink":
                        shrink += 1
                    elif status["type"] in "expand":
                        expand += 1
            dic_expand[malleable].append(expand)
            dic_shrink[malleable].append(shrink)

    res_shrink = []
    res_expand = []
    for i in ARRAY_MALLEABLE_TO_RIGID:
        arr_shrink = dic_shrink[i]
        arr_expand = dic_expand[i]

        if i == 0:
            res_shrink.append(0)
            res_expand.append(0)
        else:
            res_shrink.append((sum(arr_shrink) / len(arr_shrink)) / i)
            res_expand.append((sum(arr_expand) / len(arr_expand)) / i)

    res = {
        "Shrink Events": res_shrink,
        "Grow Events": res_expand,
    }

    if ENABLE_ENGLISH_OUTPUT:
        draw_graph_with_range(res, 0, '', 'Number of Events', "AverageEvents")
    else:
        draw_graph_with_range(res, 0, '', 'Event-Anzahl', "Event")


def node_utilization():
    dic = {}
    for item in GLOBAL_MAP:
        strategy = GLOBAL_MAP[item]["strategy"]
        malleable = GLOBAL_MAP[item]["malleable_count"]
        duration = GLOBAL_MAP[item]["duration"]
        workload = GLOBAL_MAP[item]["workload"]
        print(f"strategy: {strategy}")
        print(f"malleable: {malleable}")
        print(f"duration: {duration}")
        print(f"workload: {workload}")

        res = workload / duration
        print(f"res: {res}")
        print("##########")

        if strategy not in dic:
            dic[strategy] = {}
        if malleable not in dic[strategy]:
            dic[strategy][malleable] = []
        dic[strategy][malleable].append(res)

    res = {}
    for strategy in dic:
        res_arr = []
        for i in ARRAY_MALLEABLE_TO_RIGID:
            arr = dic[strategy][i]
            res_arr.append((sum(arr) / len(arr)) * 100)
        res[strategy] = res_arr

    if ENABLE_ORDERING:
        res = {key: res[key] for key in scheduler_algo}

    if ENABLE_ENGLISH_OUTPUT:
        draw_graph_with_range(res, 0, '', 'Node Utilization in Percent', "AverageNodeUtilization")
    else:
        draw_graph_with_range(res, 0, '', 'Nodeauslastung (%)', "Nodeauslastung")


def total_running_time():
    data = {}
    for id in GLOBAL_MAP:
        item = GLOBAL_MAP[id]
        strategy = item["strategy"]
        duration = item["duration"]
        malleable = item["malleable_count"]

        if strategy not in data:
            data[strategy] = {}
        if malleable not in data[strategy]:
            data[strategy][malleable] = []

        arr = data[strategy][malleable]

        arr.append(duration)

    print(f"ARRAY_MALLEABLE_TO_RIGID: {ARRAY_MALLEABLE_TO_RIGID}")
    print(f"data: {data}")

    res = {}
    for strategy in data:
        new_res = []
        for item in ARRAY_MALLEABLE_TO_RIGID:
            arr = data[strategy][item]
            new_res.append((sum(arr) / len(arr)) / 1000)
        res[strategy] = new_res

    if ENABLE_ORDERING:
        res = {key: res[key] for key in scheduler_algo}

    if ENABLE_ENGLISH_OUTPUT:
        draw_graph_with_range(res, DRAW_RANGE, '', 'Time in Seconds', "OverallCompletionTime")
    else:
        draw_graph_with_range(res, DRAW_RANGE, '', 'Zeit (s)', "Gesamtlaufzeit")

    for strategy in res:
        tmp_res = []
        start = res[strategy][0]
        for i in res[strategy]:
            tmp_res.append(((start - i) / start) * 100)
        res[strategy] = tmp_res[1:]

    percentage_array = [(value / ARRAY_MALLEABLE_TO_RIGID[1:][-1]) * 100 for value in ARRAY_MALLEABLE_TO_RIGID[1:]]

    if ENABLE_ENGLISH_OUTPUT:
        draw_graph_with_range(res, 0, '', 'Speedup Overall Completion Time in Percent', "SpeedupOverallCompletionTime",
                              percentage_array)
    else:
        draw_graph_with_range(res, 0, '', 'Speedup Gesamtlaufzeit (%)', "SpeedupGesamtlaufzeit", percentage_array)


def draw_stacked_graph_with_range(data_a, data_b, base_data, header_title, y_title, save_title, x_data=None):
    if x_data is None:
        percentage_array = [(value / ARRAY_MALLEABLE_TO_RIGID[-1]) * 100 for value in ARRAY_MALLEABLE_TO_RIGID]
        x_data = percentage_array

    mytitle = 'Malleable/Moldable-Job Anteil (%)'
    if ENABLE_ENGLISH_OUTPUT:
        mytitle = 'Percentage of Moldable/Evolving Jobs'

    fig = go.Figure()

    x_axis_labels = ["0", "20", "40", "60", "80", "100"]

    strategies = list(data_a.keys())

    if ENABLE_ORDERING:
        strategies = scheduler_algo
        data_a = {key: data_a[key] for key in scheduler_algo}
        data_b = {key: data_b[key] for key in scheduler_algo}

    total_strategies = len(strategies)
    bar_width = 0.15
    positions = [-(total_strategies / 2) * bar_width + i * (bar_width + 0.04) for i in range(total_strategies)]

    for i, key in enumerate(strategies):
        x_position = [int(x_axis) + positions[i] for x_axis in range(len(x_axis_labels))]

        fig.add_trace(go.Bar(
            x=x_position,
            y=data_a[key],
            marker=dict(color=color_mapping["inside_color_a"], line=dict(color=color_mapping[key], width=4)),
            width=bar_width,
            showlegend=False
        ))
        fig.add_trace(go.Bar(
            x=x_position,
            y=data_b[key],
            marker=dict(color=color_mapping["inside_color_b"], line=dict(color=color_mapping[key], width=4)),
            width=bar_width,
            showlegend=False
        ))

    fig.add_trace(go.Bar(
        x=[None],
        y=[None],
        marker=dict(color=color_mapping["inside_color_a"]),
        name="Waiting Time"
    ))

    fig.add_trace(go.Bar(
        x=[None],
        y=[None],
        marker=dict(color=color_mapping["inside_color_b"]),
        name="Computation Time"
    ))

    for key in scheduler_algo_reverse:
        fig.add_trace(go.Bar(
            x=[None],
            y=[None],
            name=key,
            marker=dict(color=color_mapping[key]),
        ))

    fig.update_layout(
        barmode='stack',
        bargap=0.15,
        bargroupgap=0.2,
        title=header_title,
        yaxis=dict(
            title=y_title,
            titlefont_size=FONTSIZE,
            tickfont_size=FONTSIZE,
        ),
        xaxis=dict(
            tickvals=list(range(len(x_axis_labels))),
            ticktext=x_axis_labels,
            title=mytitle,
            titlefont_size=FONTSIZE,
            tickfont_size=FONTSIZE,
            range=[-0.5, 5.5],
        ),

        width=WIDTH_PIXEL,
        height=HEIGHT_PIXEL,
        margin=dict(l=10, r=10, t=10, b=100),
        legend=dict(
            font=dict(size=FONTSIZE),
            traceorder="reversed",
        ),
    )

    if DRAW_WEB:
        fig.show()

    output = f"{ANALYSES_PATH}{save_title}.{FORMAT}"
    fig.write_image(output, format=OUTPUT_FILE_TYPE, engine="kaleido")

    #Write ONE csv/dat for gnuplot
    output = f"{ANALYSES_PATH}{save_title}All.dat"
    headers_waiting = ["Percentage"] + ["{}-Waiting".format(key) for key in data_a.keys()]
    headers_computation = ["{}-Computation".format(key) for key in data_b.keys()]
    headers = headers_waiting + headers_computation
    data_to_write = []
    data_to_write.append(", ".join(headers))
    for i, label in enumerate(x_axis_labels):
        row = [label]
        for dataset in [data_a, data_b]:
            for key in dataset:
                row.append(str(dataset[key][i]))
        data_to_write.append(", ".join(row))

    with open(output, 'w') as file:
        for line in data_to_write:
            file.write(line + "\n")

    #Write MULTIPLE csv for gnuplot
    data_points = ["1", "2", "3", "4", "5", "6"]
    for strategy in data_a.keys():
        output = f"{ANALYSES_PATH}{save_title}{strategy}.csv"
        with open(output, 'w') as file:
            file.write("Datapoint,{}-Waiting,{}-Computation\n".format(strategy, strategy))
            for i, label in enumerate(data_points):
                file.write("{},{},{}\n".format(label, data_a[strategy][i], data_b[strategy][i]))



def draw_graph_with_range(data, base_data, header_title, y_title, save_title, x_data=None):
    if x_data is None:
        percentage_array = [(value / ARRAY_MALLEABLE_TO_RIGID[-1]) * 100 for value in ARRAY_MALLEABLE_TO_RIGID]
        x_data = percentage_array

    mytitle = 'Malleable/Moldable-Job Anteil (%)'
    if ENABLE_ENGLISH_OUTPUT:
        mytitle = 'Percentage of Moldable/Evolving Jobs'

    fig = go.Figure()

    standoff = 10
    all_data_csv = []
    for key, values in data.items():
        y_data = [value - base_data for value in values]

        color = color_mapping.get(key, 'gray')

        fig.add_trace(go.Bar(x=x_data, y=y_data,
                             base=base_data,
                             name=key,
                             marker_color=color,
                             ))

        if max(y_data) < 1000:
            standoff = 19

        df = pd.DataFrame({
            f'{mytitle}': x_data,
            f'{key}': y_data,
        })
        all_data_csv.append(df)

    fig.update_layout(
        title=header_title,
        yaxis=dict(
            title=y_title,
            titlefont_size=FONTSIZE,
            tickfont_size=FONTSIZE,
        ),
        xaxis=dict(
            title=mytitle,
            titlefont_size=FONTSIZE,
            tickfont_size=FONTSIZE,
        ),
        width=WIDTH_PIXEL,
        height=HEIGHT_PIXEL,
        margin=dict(l=10, r=10, t=10, b=100),
        legend=dict(
            font=dict(size=FONTSIZE),
        ),
        yaxis_title_standoff=standoff,
    )

    if DRAW_WEB:
        fig.show()

    output = f"{ANALYSES_PATH}{save_title}.{FORMAT}"
    fig.write_image(output, format=OUTPUT_FILE_TYPE, engine="kaleido")

    # write csv
    combined_df = pd.concat(all_data_csv, axis=1).T.drop_duplicates().T
    combined_df = combined_df.loc[:, ~combined_df.columns.duplicated()]
    for col in combined_df.columns:
        combined_df[col] = combined_df[col].apply(lambda x: int(x) if x == int(x) else x)
    output = f"{ANALYSES_PATH}{save_title}.csv"
    combined_df.to_csv(output, index=False)


def generate_csv_per_experiment():
    csv_path = ANALYSES_PATH + "/csv_per_experiment"
    if not os.path.exists(csv_path):
        os.mkdir(csv_path)

    for key in GLOBAL_MAP:
        generate_csv(GLOBAL_MAP[key], csv_path, key)


def generate_csv(data, csv_path, slurmjobid):
    file_name = csv_path + "/" + data["strategy"] + "_" + str(
        data["rigid_count"]) + "_" + str(data["malleable_count"]) + "_" + data["id"] + ".csv"

    start = data["start"][0]

    with open(file_name, "w") as file:
        file.write(
            "id,job_name,"
            "job_class,"
            "job_type,"
            "submit,"
            "start,"
            "end,"
            "duration,"
            "startNodes,"
            "shrinkCount,"
            "expandCount,"
            "kill,"
            "errorElastic,"
            "answer")
        for key in data["jobs"]:
            job = data["jobs"][key]
            if "start" not in job:
                continue
            str_tmp = "\n"
            str_tmp += str(key) + ","
            str_tmp += str(job["JOB_NAME"]) + ","
            str_tmp += str(job["JOB_CLASS"]) + ","
            str_tmp += str(job["JOB_TYPE"]) + ","
            str_tmp += str((job["insert"] / 1000) - start) + ","
            str_tmp += str((job["start"] / 1000) - start) + ","
            str_tmp += str((job["end"] / 1000) - start) + ","
            str_tmp += str((job["end"] - job["start"]) / 1000) + ","
            str_tmp += str(len(job["nodes"])) + ","

            shrink_apgas = 0
            expand_apgas = 0
            for timestamp in job["tracking"]:
                status = job["tracking"][timestamp]
                if status["type"] in "shrink":
                    shrink_apgas += 1
                elif status["type"] in "expand":
                    expand_apgas += 1
            str_tmp += str(shrink_apgas) + ","
            str_tmp += str(expand_apgas) + ","

            str_tmp += str(job["kill"]) + ","
            str_tmp += str(job["errorElastic"]) + ","
            if job["errorElastic"] == True:
                print(str(slurmjobid) + " errorElastic==true for job " + str(key))

            if job["answer"] == 0:
                str_tmp += "0,"
            else:
                str_tmp += str((job["answer"] - job["start"]) / 1000) + ","

            file.write(str_tmp)


def draw_all_graphs():
    image_path = ANALYSES_PATH + "/images_graph_per_experiment"
    if not os.path.exists(image_path):
        os.mkdir(image_path)

    for key in GLOBAL_MAP:
        draw_graph(GLOBAL_MAP[key], image_path)


def draw_graph(data, image_path):
    plt.clf()

    plt.title(data["strategy"] + " (" + data["id"] + ")")
    plt.ylabel('[Nodes]')
    plt.xlabel('[sec]')

    node_to_graph_id = data["nodeToGraphId"]

    jobs = data["jobs"]
    start_graph_x = -1

    #print(data["id"], "Graph", data["strategy"], "Malleable:" + str(data["malleable_count"]),
    #     "Rigid:" + str(data["rigid_count"]))

    for key in data["order"]:
        job = jobs[key]
        tracking = job["tracking"]
        dic = {}

        if start_graph_x == -1:
            start_graph_x = job["start"]

        for timestamp in tracking:
            status = tracking[timestamp]
            if status["type"] in "start":
                start = (timestamp - start_graph_x) / 1000
                for nodeId in status["nodes"]:
                    dic[nodeId] = start
            elif status["type"] in "shrink":
                x2 = (timestamp - start_graph_x) / 1000
                for i in status["nodes"]:
                    y2 = node_to_graph_id[i]
                    y1 = y2 - 1
                    x1 = dic[i]
                    generate_block(x1, y1, x1, y2, x2, y2, x2, y1, color_map[key])
                    dic.pop(i)
            elif status["type"] in "expand":
                start = (timestamp - start_graph_x) / 1000
                for nodeId in status["nodes"]:
                    dic[nodeId] = start
            if status["type"] in "end":
                x2 = (timestamp - start_graph_x) / 1000
                for i in dic:
                    y2 = node_to_graph_id[i]
                    y1 = y2 - 1
                    x1 = dic[i]
                    generate_block(x1, y1, x1, y2, x2, y2, x2, y1, color_map[key])

    file_name = image_path + "/" + data["strategy"] + "_" + str(
        data["malleable_count"]) + "_" + str(data["rigid_count"]) + "_" + data["id"] + ".pdf"
    plt.savefig(file_name)


def generate_block(bottom_left_x, bottom_left_y, top_left_x, top_left_y, top_right_x, top_right_y, bottom_right_x,
                   bottom_right_y, color):
    x = [bottom_left_x, top_left_x, top_right_x, bottom_right_x]
    y = [bottom_left_y + 0.025, top_left_y - 0.025, top_right_y - 0.025, bottom_right_y + 0.025]
    plt.fill(x, y, c=color)


def read_file(path, unique_id):
    nodes = 0
    experiment_end = False
    strategy = ""
    jobs = {}
    job_count = 0
    malleable_count = 0
    rigid_count = 0
    node_to_Graph_Id = {}
    start_exp = 99999999999999999999
    end_exp = 0
    node_workload = {}
    node_start_time = {}

    with open(path, "r") as file:
        lines = [line.rstrip().split(";") for line in file]

    for line in lines:
        timestamp, command = int(line[1]), line[2]
        if "[scheduler.worker.cluster.Node:setNewNodes]" in command and "Add Node >>" in command:
            nodes = nodes + 1
            node_name = command.split(">>")[1].split(",")[0].replace("{ JobID:", "").strip()
            node_to_Graph_Id[node_name] = nodes
            node_workload[node_name] = 0
            node_start_time[node_name] = 0
        elif "[scheduler.Scheduler:main] - Mode: " in command:
            strategy = command.replace("[scheduler.Scheduler:main] - Mode: ", "").replace(" ", "")
        elif "Created sh-File" in command:
            job_count = job_count + 1
            job_id = get_job_id(command)
            jobs[job_id] = {
                "id": job_id,
                "JOB_NAME": "",
                "insert": timestamp,
                "JOB_TYPE": "",
                "JOB_CLASS": "",
                "MIN_NODES": "-",
                "MAX_NODES": "-",
                "NODES": "-",
                "PORT": "",
                "IP": "",
                "kill": False,
                "errorElastic": False,
                "answer": 0,
                "tracking": {},
            }
        elif " :: Start Job on >>" in command:
            start_exp = min(timestamp, start_exp)
            job_id = get_job_id(command)
            job = jobs[job_id]
            job["start"] = timestamp
            job_nodes = command.replace(" ", "").split(">>")[1].split(",")
            job["nodes"] = job_nodes[0:len(job_nodes) - 1]
            job["tracking"] = {
                timestamp: {"type": "start", "nodes": job["nodes"]}
            }
            for node in job_nodes:
                node_start_time[node] = timestamp
        elif " :: is done" in command and " >> " not in command:
            end_exp = max(timestamp, end_exp)
            job_id = get_job_id(command)
            job = jobs[job_id]
            job["end"] = timestamp
            job["tracking"][timestamp] = {"type": "end"}
        elif " :: is done" in command and " >> " in command:
            node = command.split(">>")[1].replace("free Node:", "").split()[0]
            time = timestamp - node_start_time[node]
            node_workload[node] = node_workload[node] + time
            node_start_time[node] = 0
        # Evolving!
        elif " :: free Node caused by Release:" in command:
            node = command.split(":")[5]
            # print(f"free Node caused by Release, node: {node}")
            time = timestamp - node_start_time[node]
            node_workload[node] = node_workload[node] + time
            node_start_time[node] = 0
            # for malleable this is done below in "elif ":: free Node:""
            # but this does not work for evolving
            job_id = get_job_id(command)
            job = jobs[job_id]
            tmp = command.split("::")
            tmp = tmp[1].split(":")[1].replace(" ", "")
            if timestamp not in job["tracking"]:
                job["tracking"][timestamp] = {
                    "type": "shrink",
                    "nodes": []
                }
            job["tracking"][timestamp]["nodes"].append(tmp)
        elif " :: is now Reachable >> Job is now Malleable" in command:
            job_id = get_job_id(command)
            job = jobs[job_id]
            job["answer"] = timestamp
        elif " :: reachable Error >> Job is now not longer Malleable" in command:
            job_id = get_job_id(command)
            job = jobs[job_id]
            job["errorElastic"] = True
        elif " :: start kill Process" in command:
            job_id = get_job_id(command)
            job = jobs[job_id]
            job["kill"] = True
        elif "JOB_NAME:" in command:
            job_id = get_job_id(command)
            job = jobs[job_id]
            tmp = command.split(":")
            job["JOB_NAME"] = tmp[len(tmp) - 1].replace(" ", "")
        elif "JOB_TYPE:" in command:
            job_id = get_job_id(command)
            job = jobs[job_id]
            tmp = command.split(":")
            job["JOB_TYPE"] = tmp[len(tmp) - 1].replace(" ", "")
        elif "JOB_CLASS:" in command:
            job_id = get_job_id(command)
            job = jobs[job_id]
            tmp = command.split(":")
            job["JOB_CLASS"] = tmp[len(tmp) - 1].replace(" ", "")
            if "rigid" in job["JOB_CLASS"]:
                rigid_count = rigid_count + 1
            else:
                malleable_count = malleable_count + 1
        elif "MIN_NODES:" in command:
            job_id = get_job_id(command)
            job = jobs[job_id]
            tmp = command.split(":")
            job["MIN_NODES"] = tmp[len(tmp) - 1].replace(" ", "")
        elif "MAX_NODES:" in command:
            job_id = get_job_id(command)
            job = jobs[job_id]
            tmp = command.split(":")
            job["MAX_NODES"] = tmp[len(tmp) - 1].replace(" ", "")
        elif "NODES:" in command and "MAX_NODES" not in command and not "MIN_NODES" in command:
            job_id = get_job_id(command)
            job = jobs[job_id]
            tmp = command.split(":")
            job["NODES"] = tmp[len(tmp) - 1].replace(" ", "")
        elif "PORT:" in command:
            job_id = get_job_id(command)
            job = jobs[job_id]
            tmp = command.split(":")
            job["PORT"] = tmp[len(tmp) - 1].replace(" ", "")
        elif "IP:" in command:
            job_id = get_job_id(command)
            job = jobs[job_id]
            tmp = command.split(":")
            job["IP"] = tmp[len(tmp) - 1].replace(" ", "")
        elif ":: free Node:" in command:
            job_id = get_job_id(command)
            job = jobs[job_id]
            tmp = command.split("::")
            tmp = tmp[1].split(":")[1].replace(" ", "")
            if timestamp not in job["tracking"]:
                job["tracking"][timestamp] = {
                    "type": "shrink",
                    "nodes": []
                }
            job["tracking"][timestamp]["nodes"].append(tmp)
        elif ":: Expand on:" in command:
            job_id = get_job_id(command)
            job = jobs[job_id]
            tmp = command.split("::")
            tmp = tmp[1].split(":")[1].replace(" ", "")
            if timestamp not in job["tracking"]:
                job["tracking"][timestamp] = {
                    "type": "expand",
                    "nodes": []
                }
            job["tracking"][timestamp]["nodes"].append(tmp)
            node = command.split("::")[1].replace("Expand on:", "").split()[0]
            node_start_time[node] = timestamp
        elif "Time" in command and "::" in command in command:
            # Sample command:
            # JobID:2 NQueens_17_malleable_1-8 :: shrinkTimeAPGAS : 1 : 3790576570 : 3.79057657 sec
            # Possible event values:
            # growTimeAPGAS
            # shrinkTimeAPGAS
            # postGrowTimeGLB
            # postShrinkTimeGLB
            # preGrowTimeGLB
            # preShrinkTimeGLB
            tmp = command.replace(" ", "").split("::")[1]
            event = tmp.split(":")[0]
            nbPlaces = tmp.split(":")[1]
            time = tmp.split(":")[2]

            if event in SHRINK_GROW_DICT:
                SHRINK_GROW_DICT[event][0].append(nbPlaces)
                SHRINK_GROW_DICT[event][1].append(time)
            else:
                SHRINK_GROW_DICT[event] = ([nbPlaces], [time])

        elif "[scheduler.logger.Logger:run] - End Tracking" in command:
            experiment_end = True

    order = []
    start = []
    count_error = 0
    for key in jobs:
        job = jobs[key]
        order.append(key)
        if "start" in job:
            start.append(job["start"] / 1000)
        else:
            count_error += 1

    if len(order) == 0 or len(start) == 0:
        print("\033[91m" + "ERROR: " + unique_id + " " + strategy + " :: There were no jobs executed" + "\033[0m")
        return
    if not experiment_end:
        print("\033[91m" + "ERROR: " + unique_id + " " + strategy + " :: Scheduler was canceled" + "\033[0m")
        return

    start, order = zip(*sorted(zip(start, order)))

    count = 0
    workload = 0
    #print(f"node_workload: {node_workload}")

    for i in node_workload:
        workload = workload + node_workload[i]
        count += 1
    workload = workload / count

    GLOBAL_MAP[unique_id] = {
        "id": unique_id,
        "nodes": nodes,
        "strategy": strategy,
        "job_count": job_count,
        "malleable_count": malleable_count,
        "rigid_count": rigid_count,
        "jobs": jobs,
        "nodeToGraphId": node_to_Graph_Id,
        "order": order,
        "start": start,
        "start_exp": start_exp,
        "end_exp": end_exp,
        "duration": end_exp - start_exp,
        "node_workload": node_workload,
        "workload": workload,
    }

    MALLEABLE_TO_RIGID_RATE.add(malleable_count)


def get_job_id(command):
    command = command.split("-", 1)[1]
    if "JobID:" in command and ":: is done" in command:
        tmp = command.replace("JobID:", "").split("::")[0].strip()
        return int(tmp)
    if "::" in command:
        tmp = command.replace("JobID:", "").split("::")[0].strip()
        return int(tmp)
    if ">>" in command:
        tmp = command.replace("JobID:", "").split(">>")[0].strip()
        return int(tmp)
    return 0


def load_config(module_name):
    try:
        config_module = importlib.import_module(module_name)
        return {var: getattr(config_module, var) for var in config_variables}
    except ImportError:
        print(f"Configuration module '{module_name}' not found.")
        sys.exit(1)

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Path to the data input required")
        exit()

    assert os.path.isdir(sys.argv[1])
    INPUT_PATH = str(sys.argv[1]).removesuffix("/") + "/"
    ANALYSES_PATH = INPUT_PATH + "_analyses/"
    SHRINK_GROW_FILE_PATH = ANALYSES_PATH + "/" + SHRINK_GROW_FILE
    if os.path.isfile(SHRINK_GROW_FILE_PATH):
        os.remove(SHRINK_GROW_FILE_PATH)

    config_module_name = "smallConfig"
    if len(sys.argv) > 2:
        config_module_name = sys.argv[2]
    config = load_config(config_module_name)
    print("Config: ", config_module_name)
    for var in config_variables:
        globals()[var] = config[var]
    scheduler_algo_reverse = scheduler_algo[::-1]

    if len(sys.argv) > 3:
        ENABLE_ENGLISH_OUTPUT = sys.argv[3].lower() == 'true'

    main()
