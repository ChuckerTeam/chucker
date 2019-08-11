package com.chuckerteam.chucker.api.internal.ui.error;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chuckerteam.chucker.R;
import com.chuckerteam.chucker.api.internal.data.entity.RecordedThrowableTuple;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class ErrorAdapter extends RecyclerView.Adapter<ErrorAdapter.ErrorViewHolder> {

    @NonNull
    private final ErrorClickListListener listener;
    @NonNull
    private final Context context;
    @NonNull
    private List<RecordedThrowableTuple> recordedThrowables = new ArrayList<>();

    ErrorAdapter(@NonNull Context context, @NonNull ErrorClickListListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ErrorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chucker_list_item_error, parent, false);
        return new ErrorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ErrorViewHolder holder, final int position) {
        final RecordedThrowableTuple throwable = this.recordedThrowables.get(position);
        holder.bind(throwable);
    }

    @Override
    public int getItemCount() {
        return recordedThrowables.size();
    }

    public void setData(List<RecordedThrowableTuple> recordedThrowables) {
        this.recordedThrowables = recordedThrowables;
        notifyDataSetChanged();
    }

    public class ErrorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView tag;
        private final TextView clazz;
        private final TextView message;
        private final TextView date;
        private RecordedThrowableTuple throwable;

        ErrorViewHolder(View itemView) {
            super(itemView);
            tag = itemView.findViewById(R.id.tag);
            clazz = itemView.findViewById(R.id.clazz);
            message = itemView.findViewById(R.id.message);
            date = itemView.findViewById(R.id.date);
            itemView.setOnClickListener(this);
        }

        void bind(RecordedThrowableTuple throwable) {
            this.throwable = throwable;
            tag.setText(throwable.getTag());
            clazz.setText(throwable.getClazz());
            message.setText(throwable.getMessage());
            date.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(throwable.getDate()));
        }

        @Override
        public void onClick(View v) {
            if (throwable != null) listener.onErrorClick(throwable.getId(), getAdapterPosition());
        }
    }

    public interface ErrorClickListListener {
        void onErrorClick(long throwableId, int position);
    }
}
