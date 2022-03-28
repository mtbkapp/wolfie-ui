import os
import sqlite3
import uuid
import time


def now():
    return int(time.time())

def fetch_all(conn, query, params=[]):
    cur = conn.cursor()
    try:
        cur.execute(query, params)
        return cur.fetchall()
    finally:
        cur.close()

def to_dicts(columns, rows):
    dicts = []
    for r in rows:
        d = {}
        for i in range(len(columns)):
            d[columns[i]] = r[i]
        dicts.append(d)
    return dicts

def all_drawings(conn):
    cols = ['drawing_id', 'data', 'updated_at']
    q = "select drawing_id, data, updated_at from drawing order by updated_at desc"
    return to_dicts(cols, fetch_all(conn, q))

def add_drawing(conn, data): 
    drawing_id = str(uuid.uuid4())
    conn.execute("insert into drawing (drawing_id, data, updated_at) values (?,?,?)", [drawing_id, data, now()])
    conn.commit();
    return drawing_id

def update_drawing(conn, drawing_id, data):
    conn.execute("update drawing set data = ?, updated_at = ? where drawing_id = ?", [data, now(), drawing_id])

def remove_drawing(conn, drawing_id):
    conn.execute("delete from drawing where drawing_id = ?", [drawing_id])
    conn.commit()

def all_sounds(conn):
    cols = ['sound_id', 'data', 'updated_at']
    q = "select sound_id, data, updated_at from sound order by updated_at desc" 
    return to_dicts(cols, fetch_all(conn, q))

def add_sound(conn, name, data):
    sound_id = str(uuid.uuid4())
    conn.execute("insert into sound (sound_id, name, data, updated_at) values (?,?,?,?)", [sound_id, name, data, now()])
    conn.commit()
    return sound_id

def remove_sound(conn, sound_id):
    conn.execute("delete from sound where sound_id = ?", [sound_id])
    conn.commit()

def config_button(conn, button_id, drawing_id, sound_id):
    assert button_id >= 1 and button_id <= 4
    conn.execute("update button set drawing_id = ?, sound_id = ? where button_id = ?", [drawing_id, sound_id, button_id]) 
    conn.commit()

def get_button_configs(conn):
    q = """
        select
            b.button_id,
            b.sound_id,
            s.data as sound_data,
            b.drawing_id,
            d.data as drawing_data
        from
            button b
            left join drawing d on (d.drawing_id = b.drawing_id)
            left join sound s on (s.sound_id = b.sound_id)
    """
    d = {}
    for r in fetch_all(conn, q):
        d[r[0]] = {
            'sound_id': r[1],
            'sound_data': r[2],
            'drawing_id': r[3],
            'drawing_data': r[4]
        }
    return d

drawing_table_create = "create table drawing (drawing_id text primary key not null, data, updated_at int)"
sound_table_create = "create table sound (sound_id text primary key not null, name, data, updated_at int)"
button_table_create = "create table button (button_id integer primary key not null, drawing_id text, sound_id text, foreign key (sound_id) references sound(sound_id) foreign key(drawing_id) references drawing(drawing_id))" 

def does_table_exist(conn, table_name):
    rows = fetch_all(conn, "select count(*) from sqlite_master where type = 'table' and tbl_name = ?", [table_name])
    return rows[0][0] == 1 

def init_schema(conn):
    print("init schema...")
    if not does_table_exist(conn, "drawing"):
        print("init drawing")
        conn.execute(drawing_table_create)
        conn.commit()
    if not does_table_exist(conn, "sound"):
        print("init sound table")
        conn.execute(sound_table_create)
        conn.commit()
    if not does_table_exist(conn, "button"):
        print("init button table")
        conn.execute(button_table_create)
        conn.execute("insert into button (button_id) values (1)")
        conn.execute("insert into button (button_id) values (2)")
        conn.execute("insert into button (button_id) values (3)")
        conn.execute("insert into button (button_id) values (4)")
        conn.commit()
    print("init schema done")


# test
if __name__ == "__main__":
    conn = sqlite3.connect(":memory:")
    try:
        init_schema(conn)
        s0 = add_sound(conn, 'fart', 'fart')
        print(s0)
        s1 = add_sound(conn, 'burp', 'burp')
        print(s1)
        s2 = add_sound(conn, 'slurp', 'slurp')
        print(s2)
        s3 = add_sound(conn, 'bark', 'bark')
        print(s3)
        s4 = add_sound(conn, 'whine', 'whine')
        remove_sound(conn, s4)
        for s in all_sounds(conn):
            print(s)
        print(get_button_configs(conn))
        d0 = add_drawing(conn, 'poop 0')
        d1 = add_drawing(conn, 'poop 1')
        d2 = add_drawing(conn, 'poop 2')
        d3 = add_drawing(conn, 'poop 3')
        d4 = add_drawing(conn, 'poop 4')
        update_drawing(conn, d4, 'poop poop poop')
        print("before remove")
        for d in all_drawings(conn):
            print(d)
        remove_drawing(conn, d4)
        print("after remove")
        for d in all_drawings(conn):
            print(d)
        config_button(conn, 1, d0, s0)
        config_button(conn, 2, d1, None)
        config_button(conn, 3, None, s1) 
        print(get_button_configs(conn))

    finally:
        conn.close()
