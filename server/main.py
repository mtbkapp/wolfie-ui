from http.server import HTTPServer, BaseHTTPRequestHandler, SimpleHTTPRequestHandler
import os
import json
import db
import sqlite3


STATIC_DIR='./public'

# get all button settings
# set one button settings
# get all drawings
# get all sounds
# add drawing
# remove drawing
# add sounds
# remove sound

conn = None

def ok_resp(body):
    return {'status': 200, 'body': body}

def get_buttons():
    return ok_resp(db.get_button_configs(conn))

def get_drawings():
    return ok_resp(db.all_drawings(conn))

def add_drawing(base64PNG):
    drawing_id = db.add_drawing(conn, base64PNG)
    return ok_resp({'drawing_id': drawing_id})

def get_sounds():
    return ok_resp(db.all_sounds(conn))

def add_sound(sound):
    sound_id = db.add_sound(conn, sound['name'], sound['data']) 
    return ok_resp({'sound_id': sound_id})

def config_button(data):
    print("CONFIG", data)
    return ok_resp({})


handlers = {
    ('GET', 'buttons'): get_buttons,
    ('GET', 'drawings'): get_drawings,
    ('POST', 'drawings'): add_drawing,
    ('GET', 'sounds'): get_sounds, 
    ('POST', 'sounds'): add_sound, 
    ('POST', 'config-button'): config_button
}

class Handler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=STATIC_DIR, **kwargs)

    def do_GET(self, **kwargs):
        if self.path.startswith("api"):
            return self.handleApiReq("GET")
        else:
            return super().do_GET(**kwargs)

    def do_POST(self):
        if self.path.startswith("api"):
            return self.handleApiReq("POST")
        else:
            self.send_response(404)
            self.sendJSON({'error': 'endpoint not found'})

    def handleApiReq(self, method):
        segments = self.path.split('/')
        segments.pop(0)
        handler_id = tuple([method] + segments)
        if handler_id in handlers:
            if (method == "POST"):
                resp = handlers[handler_id](self.recvJSON())
            else:
                resp = handlers[handler_id]()
            self.send_response(resp['status'])
            self.sendJSON(resp['body'])
        else:
            self.send_response(404)
            self.sendJSON({'error': 'endpoint not found', 'path': self.path})

    def recvJSON(self):
        content_len = int(self.headers['Content-Length'])
        return json.loads(self.rfile.read(content_len))

    def sendJSON(self, data):
        self.send_header('Content-Type', 'application/json')
        byte_data = json.dumps(data).encode(encoding='utf_8')
        self.send_header('Content-Length', len(byte_data))
        self.end_headers()
        self.wfile.write(byte_data)




if __name__ == '__main__':
    # get db connection
    conn = sqlite3.connect(":memory:")
    db.init_schema(conn)

    # start http server
    host = os.getenv('HOST') or ''
    port = os.getenv('PORT') or 8000
    httpd = HTTPServer((host, port), Handler)
    print('Server started on ' + host + ':' + str(port)) 
    httpd.serve_forever()
