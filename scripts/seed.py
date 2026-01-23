import psycopg2
import random
from faker import Faker
from datetime import datetime

# Configuración
fake = Faker('es_ES')
conn_params = {
    "host": "localhost",
    "database": "devdb",
    "user": "ups",
    "password": "ups123",
    "port": "5432"
}

def seed_database():
    conn = None
    try:
        conn = psycopg2.connect(**conn_params)
        cur = conn.cursor()
        print("Conectado a la base de datos en Docker...")

        # 1. Limpiar tablas (Usamos los nombres que Hibernate suele crear)
        # Si \dt te mostró 'categorias', cambia 'categories' por 'categorias' abajo
        cur.execute("TRUNCATE product_categories, products, categories, users CASCADE;")

        # 2. Insertar 5 Usuarios
        print("Insertando usuarios...")
        users_data = [
            (1, 'Admin Ups', 'admin@ups.edu.ec', 'pass123', False, datetime.now(), datetime.now()),
            (2, 'Pablo Torres', 'ptorres@ups.edu.ec', 'pass123', False, datetime.now(), datetime.now()),
            (3, 'Juan Perez', 'jperez@gmail.com', 'pass123', False, datetime.now(), datetime.now()),
            (4, 'Maria Garcia', 'mgarcia@yahoo.com', 'pass123', False, datetime.now(), datetime.now()),
            (5, 'Mateo Backend', 'mateo@ups.edu.ec', 'pass123', False, datetime.now(), datetime.now())
        ]
        cur.executemany("""
            INSERT INTO users (id, name, email, password, deleted, created_at, updated_at) 
            VALUES (%s, %s, %s, %s, %s, %s, %s)
        """, users_data)

        # 3. Insertar 5 Categorías (Cambiado a 'categories' según tu error)
        print("Insertando categorías...")
        categories_data = [
            (1, 'Electrónica', 'Gadgets y dispositivos', False, datetime.now(), datetime.now()),
            (2, 'Gaming', 'Hardware de alto rendimiento', False, datetime.now(), datetime.now()),
            (3, 'Hogar', 'Para la casa', False, datetime.now(), datetime.now()),
            (4, 'Software', 'Licencias y apps', False, datetime.now(), datetime.now()),
            (5, 'Oficina', 'Insumos laborales', False, datetime.now(), datetime.now())
        ]
        cur.executemany("""
            INSERT INTO categories (id, name, description, deleted, created_at, updated_at) 
            VALUES (%s, %s, %s, %s, %s, %s)
        """, categories_data)

        # 4. Generar 1000 Productos
        print("Generando 1000 productos...")
        for i in range(1, 1001):
            name = fake.catch_phrase()
            price = round(random.uniform(10.0, 5000.0), 2)
            user_id = random.randint(1, 5)
            
            cur.execute("""
                INSERT INTO products (id, name, description, price, stock, user_id, created_at, updated_at, deleted)
                VALUES (%s, %s, %s, %s, %s, %s, NOW(), NOW(), false)
            """, (i, name, fake.sentence(), price, random.randint(1, 100), user_id))
            
            # Asignar 2 categorías aleatorias
            selected_cats = random.sample(range(1, 6), 2)
            for cat_id in selected_cats:
                cur.execute("""
                    INSERT INTO product_categories (product_id, category_id) 
                    VALUES (%s, %s)
                """, (i, cat_id))

        conn.commit()
        print("¡Éxito total! 1000 productos insertados.")

    except Exception as error:
        print(f"Error detectado: {error}")
        if conn: conn.rollback()
    finally:
        if conn:
            cur.close()
            conn.close()

if __name__ == "__main__":
    seed_database()