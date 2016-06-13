
def create_element(from, to, up=None, down=None, north=None, south=None, east=None, west=None):
    data = dict(from=from, to=to)
    for x in ['up', 'down', 'north', 'south', 'east', 'west']:
        if x:
            data[x] = locals()[x]
    return data

def main():
    top = create_element(from=(7,14,7), to=)

if __name__ == '__main__':
    main()