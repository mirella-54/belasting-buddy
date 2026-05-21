import os
import shutil

SOURCE_FILE = 'sample-doc.pdf'
OUTPUT_DIR = 'test_documents'
NUM_FILES = 1000

def create_unique_documents(source_file, output_dir, num_files):
    """
    Creates multiple copies of a source file, renaming them and appending 
    a unique identifier to ensure unique file hashes.
    """
    if not os.path.exists(source_file):
        print(f"Error: Source file not found: {source_file}")
        print("Please ensure 'sample-doc.pdf' is in the same directory.")
        return

    os.makedirs(output_dir, exist_ok=True)
    
    with open(source_file, 'rb') as f:
        original_content = f.read()

    print(f"Creating {num_files} unique files in: {output_dir}/")

    for i in range(1, num_files + 1):
        new_filename = f'document_{i}.pdf'
        output_path = os.path.join(output_dir, new_filename)
        
        unique_footer = f'--UNIQUE_ID: {i}--'.encode('utf-8')
        
        with open(output_path, 'wb') as f:
            f.write(original_content)
            f.write(unique_footer)
            
        if i % 10 == 0:
            print(f"  ...Created {i} files.")

    print(f"Success! Created {num_files} unique documents.")

if __name__ == "__main__":
    create_unique_documents(SOURCE_FILE, OUTPUT_DIR, NUM_FILES)