import networkx as nx
from parse import read_input_file, write_output_file
from utils import is_valid_network, average_pairwise_distance
 
import sys
import os
import operator
import random
import copy

def solve(G):
    """
    Args:
        G: networkx.Graph

    Returns:
        T: networkx.Graph
    """
    G_new = nx.Graph()
    ds = []
    #Find optimal node to begin.
    starting_node = find_optimal_node(G)
    last_node = starting_node

    # Add optimal node to disjoint set & new graph.
    G_new.add_node(starting_node)
    ds.append(starting_node)
    t = find_neighbors(G,last_node,ds,G_new,0)
    return t

def find_neighbors(G,node,ds,G_new,recursion_count):

    #Centroids
    dict_of_centrality = nx.degree_centrality(G)

    #Find all neighbors of nodes.
    neighbors = G.edges.data('weight')._adjdict[node]
    dictionary_of_candidates = {}
  
    #Pick out the best neighbor to explore, and add to G_new & DS
    for i in list(neighbors):
        if (i in ds):
            continue
        else:
            degree = 1/(G.degree[i])
            weight = neighbors[i]['weight']
            dictionary_of_candidates[i] = degree+weight 

    
    if (is_valid_network(G,G_new) == True):
        return G_new
        
    # if (recursion_count >= 900):
    #     raise Exception("Recursion depth nearing, auto-terminate.")

    #If exploring neighbors, all of them have been visited.
    #Pick a random node to explore
    if (len(dictionary_of_candidates) == 0):
        rand = random.choice(list(neighbors.keys()))
        best_candidate = rand
       # backup_candidates = {}
        #for n in neighbors.keys():
           # backup_candidates[n] = dict_of_centrality[n]
        #best_candidate = max(backup_candidates.items(),key=operator.itemgetter(1))[0]
        return  find_neighbors(G,best_candidate,ds,G_new,recursion_count+1)
    else:
        #Otherwise keep exploring
        best_candidate = min(dictionary_of_candidates.items(), key = lambda x: x[1])
                
        while (best_candidate[0] in ds):
            del dictionary_of_candidates[best_candidate[0]]
        best_candidate = min(dictionary_of_candidates.items(), key = lambda x: x[1])
        ds.append(best_candidate[0]) 
        G_new.add_edge(node,best_candidate[0],weight=best_candidate[1])
        return find_neighbors(G,best_candidate[0],ds,G_new,recursion_count)
    
    # Return G_new when done.
    return G_new


def find_optimal_node(G):
    dict_of_centrality = nx.degree_centrality(G)
    return max(dict_of_centrality.items(), key=operator.itemgetter(1))[0]

def check_if_leaf(G):
    #Checks if node is a leaf in a graph.
    set_of_normal_nodes = set()
    set_of_leaf_nodes = set()
    edges = G.edges.data('weight',default = 1)
    for i in edges:
        u = i[0]
        v = i[1]
        if (u in set_of_normal_nodes):
            set_of_leaf_nodes.add(u)
        elif (u not in set_of_normal_nodes):
            set_of_normal_nodes.add(u)

        if (v in set_of_normal_nodes):
            set_of_leaf_nodes.add(v)
        elif (u not in set_of_normal_nodes):
            set_of_normal_nodes.add(u)

    return set_of_leaf_nodes
     

def mstPruned(G):
    networkNodes = []
    leaf_nodes = []
    validNetworkNodes = dict() #containing key = Node, Value = how many edges in MST
    for edge in G.edges():
    #edge should be in (u,v) format edge[0] = u node. edge[1] = v
        u = edge[0]
        v = edge[1]
        if u in validNetworkNodes:
            validNetworkNodes[u] = validNetworkNodes.get(u) + 1
        else:
            validNetworkNodes[u] = 1

        if v in validNetworkNodes:
            validNetworkNodes[v] = validNetworkNodes.get(v) + 1
        else:
            validNetworkNodes[v] = 1

    
    for x in validNetworkNodes:
        if validNetworkNodes.get(x) >= 2:
            networkNodes.append(x)
        else:
            leaf_nodes.append(x)

    #creating our output T Graph
    T = nx.Graph()
    T.add_nodes_from(networkNodes) #adding network nodes into output T Graph

    for edge in G.edges():
        u = edge[0]
        v = edge[1]
        if u in networkNodes and v in networkNodes:
            T.add_edge(u, v, weight= G[u][v]['weight'])
    solDict = dict([(T, average_pairwise_distance(T))]) 
    # Graph T should now contain all the nodes in initial network and all the edges connections
    
    #optimizes by checking if adding leaf nodes one by one creats lower average distance
    for i in leaf_nodes:
        copyT = nx.Graph()
        networkNodes.append(i)
        copyT.add_nodes_from(networkNodes)
        for edge in G.edges():
            u = edge[0]
            v = edge[1]
            if u in networkNodes and v in networkNodes:
                copyT.add_edge(u, v, weight= G[u][v]['weight'])

        if average_pairwise_distance(copyT) < average_pairwise_distance(T):
            solDict[copyT] = average_pairwise_distance(copyT)

    return min(solDict, key=solDict.get)

##Run this for debugging individual files. 
# if __name__ == '__main__':
#     #path = 'inputs/small-60.in'
#     path = 'inputs/large-327.in'
#     G = read_input_file(path)
#     try:
#         T = solve(G)
#         T = nx.minimum_spanning_tree(T)
#         # leaf_nodes = check_if_leaf(T) #Data type: Set
#         # for i in leaf_nodes:
#         #     T.remove_node(i)
#     except:
#         T = nx.minimum_spanning_tree(G)
#         leaf_nodes = check_if_leaf(T) #Data type: Set
#         for i in leaf_nodes:
#             T.remove_node(i)
#     finally:   
#         assert is_valid_network(G, T)
#         #write_output_file(T, "submission/small-254.out")
#         print("Average  pairwise distance: {}".format(average_pairwise_distance(T)))
       

## Run this to run on all files, and prepare output for submission.
# if __name__ == "__main__":
#     output_dir = "submission"
#     input_dir = "inputs"
#     for input_path in os.listdir(input_dir):
#         graph_name = input_path.split(".")[0]
#         G = read_input_file(f"{input_dir}/{input_path}")

#         #mst = nx.minimum_spanning_tree(G)
#         #print(input_path)
#         T = solve(G)
#         write_output_file(T, f"{output_dir}/{graph_name}.out")

def advanced_prunning(G,T):
    nodes = list(G.nodes)
    for i in nodes:
        temp = copy.deepcopy(T)
        temp2 = copy.deepcopy(temp)
        temp.remove_node(i)
        if (is_valid_network(G,temp)):
            T.remove_node(i)
        else:
            temp = copy.deepcopy(temp2)
    return T


if __name__ == "__main__":
    output_dir = "submission"
    input_dir = "inputs"
    distance = []
    for input_path in os.listdir(input_dir):
        graph_name = input_path.split(".")[0]
        G = read_input_file(f"{input_dir}/{input_path}")
        mst = nx.minimum_spanning_tree(G)
        T_anita = mstPruned(mst)
        try:
            T_kevin = solve(G)
            T_kevin = nx.minimum_spanning_tree(T_kevin)
            T_kevin = advanced_prunning(G,T_kevin)
        except:
            T_kevin = nx.minimum_spanning_tree(G)
            #T_kevin = advanced_prunning(G,T_kevin)
        finally:
            assert(is_valid_network(G,T_kevin)) == True
            T_kevin_distance = average_pairwise_distance(T_kevin)
            T_anita_distance = average_pairwise_distance(T_anita)

            if (T_anita_distance < T_kevin_distance):
                T = T_anita
                distance.append(T_anita_distance)
            else:
                #
                T = T_kevin
                distance.append(T_kevin_distance)
        write_output_file(T, f"{output_dir}/{graph_name}.out")
    print(sum(distance))
            
    
   
      
    


