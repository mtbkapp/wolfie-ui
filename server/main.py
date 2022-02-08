from http.server import HTTPServer, BaseHTTPRequestHandler, SimpleHTTPRequestHandler
import os
import json


STATIC_DIR='./public'

# get all button settings
# set one button settings
# get all drawings
# get all sounds
# add drawing
# remove drawing
# add sounds
# remove sound





class Handler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=STATIC_DIR, **kwargs)

    # TODO figure out how to override do_GET?

    def do_POST(self):
        print("post to: " + self.path)
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        self.end_headers()
        content_len = int(self.headers['Content-Length'])
        data = json.loads(self.rfile.read(content_len))
        self.wfile.write(json.dumps({'path': self.path, 'data': data}).encode(encoding='utf_8'))



if __name__ == '__main__':
    host = os.getenv('HOST') or ''
    port = os.getenv('PORT') or 8000
    httpd = HTTPServer((host, port), Handler)
    print('Server started on ' + host + ':' + str(port)) 
    httpd.serve_forever()
