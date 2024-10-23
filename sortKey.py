keys=[]
for i in range(10000):
    keys.append("key_"+str(i))
keys.sort()

for i in range(10):
    print("String start"+str(i)+"=\""+keys[i*1000]+"\";")
    print("String end"+str(i)+"=\""+keys[(i+1)*1000-1]+"\";")