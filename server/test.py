import http.client
import json

def postJSON(conn, url, data):
    conn.request('POST', url, json.dumps(data).encode(encoding='utf_8'))
    resp = conn.getresponse()
    return json.loads(resp.read())

def getJSON(conn, url):
    conn.request('GET', url)
    resp = conn.getresponse()
    return json.loads(resp.read())


if __name__ == '__main__':
    conn = http.client.HTTPConnection("localhost", 8000)

    # get current button configs
    print(getJSON(conn, 'api/buttons'))

    # get all drawings
    print(getJSON(conn, 'api/drawings'))

    # post a new drawing
    print(postJSON(conn, 'api/drawings', 'base64png'))

    # get all drawings
    print(getJSON(conn, 'api/drawings'))

    # get all sounds
    print(getJSON(conn, 'api/sounds'))

    # post a new sound
    print(postJSON(conn, 'api/sounds', {'name': 'fart', 'data': 'fart fart'}))

    # get all sounds
    print(getJSON(conn, 'api/sounds'))

    conn.close()
