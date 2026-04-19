import { useRef, useState } from 'react';

interface FileUploadProps {
  label: string;
  accept?: string;
  onFile: (file: File) => void;
}

export default function FileUpload({ label, accept = '.yaml,.yml,.json', onFile }: FileUploadProps) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [fileName, setFileName] = useState<string | null>(null);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setFileName(file.name);
      onFile(file);
    }
  };

  return (
    <div
      onClick={() => inputRef.current?.click()}
      className="border-2 border-dashed border-slate-600 hover:border-indigo-500 rounded-xl p-8 text-center cursor-pointer transition-colors group"
    >
      <input ref={inputRef} type="file" accept={accept} onChange={handleChange} className="hidden" />
      <div className="text-4xl mb-3">📄</div>
      <p className="text-slate-300 group-hover:text-indigo-300 font-medium">
        {fileName ?? label}
      </p>
      <p className="text-slate-500 text-sm mt-1">
        {fileName ? 'Click to change file' : `Accepts ${accept}`}
      </p>
    </div>
  );
}
